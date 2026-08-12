import { h, Fragment } from "preact";
import { useState, useEffect, useMemo, useRef } from "preact/hooks";
import { WsEventBus } from "../ws/WSEventBus";

/**
 * Autos tab: a field visualiser and a path/auto cross-reference map.
 *
 * Geometry and analysis come from /dynamic/autoanalysis.json, written by
 * frc.robot.Utils.PathPlannerAnalysis at startup. Only the armed auto name
 * and rescan requests travel over the websocket.
 */

interface Props {
  socket: WsEventBus;
  id: number;
}

const BLUE = "rgb(68,142,205)";
const GREEN = "rgba(116,255,6,0.85)";
const ORANGE = "#ff9f43";
const RED = "#ff5c5c";
const DIM = "#8b93a3";

/* ------------------------------------------------------------------ math */

function bez(p0: number[], p1: number[], p2: number[], p3: number[], t: number) {
  const u = 1 - t;
  return [
    u * u * u * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t * t * t * p3[0],
    u * u * u * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t * t * t * p3[1],
  ];
}

/** Field XY at a waypointRelativePos, evaluating the actual bezier. */
function ptAt(wps: any[], rel: number): number[] {
  const n = wps.length - 1;
  if (n < 1) return [wps[0].ax, wps[0].ay];
  const r = Math.max(0, Math.min(rel, n));
  const seg = Math.min(Math.floor(r), n - 1);
  const t = r - seg;
  const a = wps[seg], b = wps[seg + 1];
  const p0 = [a.ax, a.ay];
  const p3 = [b.ax, b.ay];
  const p1 = a.ncx !== undefined ? [a.ncx, a.ncy] : p0;
  const p2 = b.pcx !== undefined ? [b.pcx, b.pcy] : p3;
  return bez(p0, p1, p2, p3, t);
}

function densify(wps: any[], step = 0.03) {
  const n = Math.max(wps.length - 1, 1);
  const out: number[][] = [];
  for (let r = 0; r < n; r += step) {
    const p = ptAt(wps, r);
    out.push([r, p[0], p[1]]);
  }
  const e = ptAt(wps, n);
  out.push([n, e[0], e[1]]);
  return out;
}

/** Interpolate a rotation track the short way round, like PathPlanner does. */
function headingOf(rot: number[][], local: number) {
  if (!rot || !rot.length) return 0;
  if (local <= rot[0][0]) return rot[0][1];
  if (local >= rot[rot.length - 1][0]) return rot[rot.length - 1][1];
  for (let i = 0; i < rot.length - 1; i++) {
    if (local >= rot[i][0] && local <= rot[i + 1][0]) {
      const sp = rot[i + 1][0] - rot[i][0];
      const t = sp > 1e-9 ? (local - rot[i][0]) / sp : 0;
      const d = ((((rot[i + 1][1] - rot[i][1]) % 360) + 540) % 360) - 180;
      return rot[i][1] + d * t;
    }
  }
  return rot[rot.length - 1][1];
}

/* ------------------------------------------------------------- component */

export default function ({ socket, id }: Props) {
  const [data, setData] = useState<any>(null);
  const [err, setErr] = useState<string>("");
  const [sub, setSub] = useState<"field" | "map">("field");
  const [query, setQuery] = useState("");
  const [auto, setAuto] = useState<string>("");
  const [selPath, setSelPath] = useState<string>("");
  const [armed, setArmed] = useState<string>("");
  const [flash, setFlash] = useState<string>("");
  const [playing, setPlaying] = useState(false);
  const [pos, setPos] = useState(0);          // metres along the route
  const raf = useRef<number>(0);
  const last = useRef<number>(0);

  /* ---- load analysis ---- */
  const load = () => {
    fetch("/dynamic/autoanalysis.json?t=" + Date.now())
      .then((r) => (r.ok ? r.json() : Promise.reject(r.status)))
      .then((j) => {
        setData(j);
        setErr("");
        setAuto((cur) => cur && j.autos[cur] ? cur : Object.keys(j.autos).sort()[0] || "");
      })
      .catch((e) => setErr("could not load /dynamic/autoanalysis.json (" + e + ")"));
  };
  useEffect(load, []);

  /* ---- socket feedback ---- */
  useEffect(() => {
    const off = socket.subscribe(id, (msg: string) => {
      if (msg.startsWith("armed:")) {
        const n = msg.slice(6);
        setArmed(n);
        if (n) setFlash("armed " + n);
      } else if (msg.startsWith("refused:")) {
        // payload is "<name>|<reason>"; older builds sent just "<name>"
        const body = msg.slice(8);
        const cut = body.indexOf("|");
        const who = cut < 0 ? body : body.slice(0, cut);
        const why = cut < 0 ? "it will not load" : body.slice(cut + 1);
        setFlash("REFUSED " + who + " — " + why);
      } else if (msg === "rescanned") {
        setFlash("rescanned");
        load();
      }
      setTimeout(() => setFlash(""), 4000);
    });
    // Ask the robot what is armed — it only pushes on change, so without this
    // a page refresh would show nothing armed when something actually is.
    socket.send(id, "hello");
    const retry = setTimeout(() => socket.send(id, "hello"), 1200);
    return () => { off(); clearTimeout(retry); };
  }, [socket, id]);

  /* ---- geometry for the selected auto ---- */
  const geo = useMemo(() => {
    if (!data || !auto || !data.autos[auto]) return null;
    const a = data.autos[auto];
    const segs = a.segs.map((s: any) => {
      const p = data.paths[s.n];
      return { ...s, wps: p ? p.wps : [], rot: p ? p.rot : [], poly: p ? densify(p.wps) : [] };
    });
    // one flat polyline in global relpos, plus cumulative arc length
    const flat: number[][] = [];
    segs.forEach((s: any) => s.poly.forEach((q: number[]) => flat.push([s.s + q[0], q[1], q[2]])));
    let acc = 0;
    const arc = flat.map((q, i) => {
      if (i) acc += Math.hypot(q[1] - flat[i - 1][1], q[2] - flat[i - 1][2]);
      return { s: acc, g: q[0], x: q[1], y: q[2] };
    });
    return { a, segs, flat, arc, len: acc || 1 };
  }, [data, auto]);

  useEffect(() => { setPos(0); setPlaying(false); }, [auto]);

  /* ---- playback ---- */
  useEffect(() => {
    if (!playing || !geo) return;
    last.current = 0;
    const tick = (t: number) => {
      const dt = last.current ? (t - last.current) / 1000 : 0;
      last.current = t;
      setPos((p) => {
        const n = p + 2.2 * dt;
        if (n >= geo.len) { setPlaying(false); return geo.len; }
        return n;
      });
      raf.current = requestAnimationFrame(tick);
    };
    raf.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf.current);
  }, [playing, geo]);

  if (err) {
    return <div style="padding:1rem;color:#ff5c5c">{err}
      <div style={{ color: DIM, marginTop: ".5rem", fontSize: ".85rem" }}>
        Restart robot code, or press Rescan once it is up.</div></div>;
  }
  if (!data) return <div style="padding:1rem;color:#8b93a3">loading autos…</div>;

  // Analysis ran but found nothing. Say so instead of sitting on "loading"
  // forever — this means the deploy folder is empty or in the wrong place.
  if (!Object.keys(data.autos || {}).length) {
    return (
      <div style="padding:1rem">
        <div style={{ color: RED, fontWeight: 600 }}>No autos found.</div>
        <div style={{ color: DIM, marginTop: ".4rem", fontSize: ".85rem", lineHeight: 1.5 }}>
          The analysis ran but read {Object.keys(data.paths || {}).length} paths and 0 autos from
          <code style="margin:0 .3rem">src/main/deploy/pathplanner/</code>.
          Check that <code>paths/</code> and <code>autos/</code> still exist there — closing
          PathPlanner and reopening it can empty them if files were renamed underneath it.
        </div>
        <button type="button" onClick={() => socket.send(id, "rescan")}
          style={{ marginTop: ".6rem", background: "#232a35", color: "#e6e9ef",
                   border: "1px solid #2d333d", borderRadius: "6px", padding: ".3rem .8rem" }}>
          Rescan
        </button>
      </div>
    );
  }

  if (!geo) return <div style="padding:1rem;color:#8b93a3">loading autos…</div>;

  const FX = data.field.x, FY = data.field.y;
  const R = data.robot;
  const q = query.toLowerCase().trim();
  const autoNames = Object.keys(data.autos).sort();
  const pathNames = Object.keys(data.paths).sort();

  /* ---- current playhead state ---- */
  let lo = 0, hi = geo.arc.length - 1;
  while (lo < hi) { const m = (lo + hi) >> 1; if (geo.arc[m].s < pos) lo = m + 1; else hi = m; }
  const here = geo.arc[lo];
  const g = here.g;
  const seg = geo.segs.find((s: any) => g >= s.s - 1e-9 && g <= s.e + 1e-9) || geo.segs[geo.segs.length - 1];
  const inSpan = (sp: any) => g >= sp.a && (g < sp.b || (sp.open && g <= sp.b));
  const intakeDown = geo.a.intake.some(inSpan);
  const shotNow = geo.a.shoot.find(inSpan);
  const hdg = seg ? headingOf(seg.rot, g - seg.s) : 0;
  const nextEv = geo.a.ev.find((e: any) => e.g > g);

  /* ---- field svg ---- */
  const SC = 46, PAD = 8;
  const W = FX * SC + PAD * 2, H = FY * SC + PAD * 2;
  const X = (x: number) => PAD + x * SC;
  const Y = (y: number) => PAD + (FY - y) * SC;
  const ptsBetween = (a: number, b: number) =>
    geo.flat.filter((p) => p[0] >= a - 1e-9 && p[0] <= b + 1e-9)
      .map((p) => X(p[1]).toFixed(1) + "," + Y(p[2]).toFixed(1)).join(" ");

  const lxp = (v: number) => v * SC, lyp = (v: number) => -v * SC;

  const robotArt = () => {
    const els: any[] = [];
    els.push(<rect x={lxp(R.ox) - (R.l * SC) / 2} y={lyp(R.oy) - (R.w * SC) / 2}
      width={R.l * SC} height={R.w * SC} rx="4"
      fill={BLUE} fill-opacity="0.15" stroke={BLUE} stroke-width="2" />);
    (R.features || []).forEach((f: any, i: number) => {
      const d = f.data || {};
      const sw = Math.max((d.strokeWidth || 0.01) * SC, 1.2);
      const isIntake = /intake/i.test(f.name || "");
      const isTurret = /turret/i.test(f.name || "");
      const col = isIntake ? GREEN : isTurret ? ORANGE : "#cdd4e0";
      const op = isIntake ? (intakeDown ? 1 : 0.25) : isTurret ? (shotNow ? 1 : 0.25) : 0.8;
      if (f.type === "line" && d.start && d.end) {
        els.push(<line key={i} x1={lxp(d.start.x)} y1={lyp(d.start.y)}
          x2={lxp(d.end.x)} y2={lyp(d.end.y)} stroke={col} stroke-width={sw}
          stroke-opacity={op} stroke-linecap="round" />);
      } else if (f.type === "circle" && d.center) {
        els.push(<circle key={i} cx={lxp(d.center.x)} cy={lyp(d.center.y)} r={(d.radius || 0) * SC}
          fill={d.filled ? col : "none"} fill-opacity="0.3" stroke={col}
          stroke-width={sw} stroke-opacity={op} />);
      } else if ((f.type === "rounded_rect" || f.type === "rect") && d.center && d.size) {
        const L = (d.size.length || 0) * SC, Wd = (d.size.width || 0) * SC;
        els.push(<rect key={i} x={lxp(d.center.x) - L / 2} y={lyp(d.center.y) - Wd / 2}
          width={L} height={Wd} rx={(d.borderRadius || 0) * SC}
          fill={d.filled ? col : "none"} fill-opacity="0.3" stroke={col}
          stroke-width={sw} stroke-opacity={op} />);
      }
    });
    els.push(<polygon points={`${lxp(R.l / 2 + 0.05)},0 ${lxp(R.l / 2 - 0.1)},${lyp(0.1)} ${lxp(R.l / 2 - 0.1)},${lyp(-0.1)}`} fill={BLUE} />);
    return els;
  };

  const armIt = (name: string) => {
    if (data.autos[name].errors > 0) {
      setFlash("cannot arm " + name + " — it has errors");
      setTimeout(() => setFlash(""), 4000);
      return;
    }
    socket.send(id, "arm:" + name);
  };

  /* ---- shared bits ---- */
  const subTab = (k: "field" | "map", label: string) => (
    <button type="button" class={"tab-selector " + (sub === k ? "bubble" : "")}
      style={{ fontWeight: sub === k ? 500 : 400, fontSize: ".9rem" }}
      onClick={() => setSub(k)}>{label}</button>
  );

  const chip = (k: string, v: any, col?: string) => (
    <div style={{
      background: "rgba(0,0,0,.25)", border: "1px solid #2d333d", borderRadius: "8px",
      padding: ".25rem .6rem", minWidth: "6rem",
    }}>
      <div style={{ fontSize: ".6rem", letterSpacing: ".08em", textTransform: "uppercase", color: DIM }}>{k}</div>
      <div style={{ fontSize: ".95rem", fontWeight: 600, color: col || "#e6e9ef" }}>{v}</div>
    </div>
  );

  const rowStyle = (on: boolean, lit: boolean, dim: boolean) => ({
    display: "flex", alignItems: "center", gap: ".4rem", padding: ".15rem .45rem",
    borderRadius: "5px", cursor: "pointer", fontSize: ".82rem",
    background: on ? BLUE : lit ? "#213047" : "transparent",
    color: on ? "#0d1014" : "#e6e9ef",
    opacity: dim ? 0.25 : 1,
  });

  /* =============================================================== FIELD */
  const fieldView = (
    <div style="display:flex;gap:.6rem;align-items:flex-start">
      {/* auto picker */}
      <div style="flex:0 0 13rem;max-height:34rem;overflow-y:auto">
        {autoNames.filter((n) => !q || n.toLowerCase().includes(q)).map((n) => {
          const d = data.autos[n];
          return (
            <div key={n} style={rowStyle(n === auto, false, false)}
              onClick={() => { setAuto(n); setSelPath(""); }}>
              <span style={{
                width: "6px", height: "6px", borderRadius: "50%", flex: "none",
                background: d.errors ? RED : d.warns ? "#ffd166" : GREEN,
              }} />
              <span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{n}</span>
              {armed === n && <span style={{ fontSize: ".65rem", color: GREEN }}>ARMED</span>}
            </div>
          );
        })}
      </div>

      {/* field + playback */}
      <div style="flex:1;min-width:0">
        <svg viewBox={`0 0 ${W} ${H}`} style="width:100%;height:auto;background:#0d1014;border-radius:8px">
          {/* Field render, stretched to the field rectangle so path coordinates
              line up with it. If features look offset, FIELD_X / FIELD_Y in
              PathPlannerAnalysis.java are the numbers to adjust. */}
          <image href="/field2026.png" x={PAD} y={PAD}
            width={FX * SC} height={FY * SC} preserveAspectRatio="none" opacity="0.9" />
          <rect x={PAD} y={PAD} width={FX * SC} height={FY * SC} fill="none" stroke="#3a4150" />
          {geo.segs.map((s: any, i: number) => (
            <polyline key={i} points={ptsBetween(s.s, s.e)} fill="none"
              stroke="#5b6472" stroke-width="2.5" stroke-linecap="round" />
          ))}
          {geo.a.intake.map((sp: any, i: number) => (
            <polyline key={"i" + i} points={ptsBetween(sp.a, sp.b)} fill="none"
              stroke={GREEN} stroke-width="7" stroke-opacity="0.5" stroke-linecap="round" />
          ))}
          {geo.a.shoot.map((sp: any, i: number) => (
            <polyline key={"s" + i} points={ptsBetween(sp.a, sp.b)} fill="none"
              stroke={ORANGE} stroke-width="3.5" stroke-linecap="round" />
          ))}
          {geo.a.ev.map((e: any, i: number) => {
            const p = data.paths[e.p];
            if (!p) return null;
            const xy = ptAt(p.wps, e.l);
            const isI = /Intake/.test(e.n);
            return <circle key={i} cx={X(xy[0])} cy={Y(xy[1])} r="3.6"
              fill={isI ? GREEN : ORANGE} stroke="#0d1014" stroke-width="1.2">
              <title>{e.n + "\n" + e.p + " @" + e.l.toFixed(2)}</title></circle>;
          })}
          <g transform={`translate(${X(here.x).toFixed(1)},${Y(here.y).toFixed(1)})`}>
            <g transform={`rotate(${(-hdg).toFixed(2)})`}>{robotArt()}</g>
          </g>
        </svg>

        <div style="display:flex;gap:.5rem;align-items:center;margin-top:.4rem;flex-wrap:wrap">
          <button type="button" class="btn btn-sm" style={{ background: playing ? BLUE : "#232a35", color: playing ? "#0d1014" : "#e6e9ef", border: "1px solid #2d333d", borderRadius: "6px", padding: ".2rem .7rem" }}
            onClick={() => setPlaying(!playing)}>{playing ? "❚❚" : "▶"}</button>
          <button type="button" style={{ background: "#232a35", color: "#e6e9ef", border: "1px solid #2d333d", borderRadius: "6px", padding: ".2rem .6rem" }}
            onClick={() => { setPlaying(false); setPos(0); }}>↺</button>
          <input type="range" min="0" max="1000" value={Math.round((pos / geo.len) * 1000)}
            style="flex:1;min-width:8rem;accent-color:rgb(68,142,205)"
            onInput={(e: any) => { setPlaying(false); setPos((e.target.value / 1000) * geo.len); }} />
          <span style={{ color: DIM, fontSize: ".78rem" }}>{pos.toFixed(1)}/{geo.len.toFixed(1)} m</span>
        </div>

        <div style="display:flex;gap:.4rem;margin-top:.5rem;flex-wrap:wrap">
          {chip("Path", seg ? seg.n : "—")}
          {chip("At", "@" + (g - (seg ? seg.s : 0)).toFixed(2))}
          {chip("Heading", ((((hdg % 360) + 540) % 360) - 180).toFixed(0) + "°")}
          {chip("Intake", intakeDown ? "DOWN" : "up", intakeDown ? GREEN : undefined)}
          {chip("Shooter", shotNow ? "shot #" + shotNow.n : "off", shotNow ? ORANGE : undefined)}
          {chip("Next", nextEv ? nextEv.n : "end")}
        </div>
      </div>

      {/* detail + arm */}
      <div style="flex:0 0 15rem">
        <div style={{ fontSize: "1rem", fontWeight: 600 }}>{auto}</div>
        <div style={{ color: DIM, fontSize: ".78rem", marginBottom: ".4rem" }}>
          {geo.a.segs.length} paths · {geo.a.ev.length} markers · {geo.a.shots} shots
        </div>
        <button type="button" disabled={geo.a.errors > 0}
          style={{
            width: "100%", padding: ".35rem", borderRadius: "8px", fontWeight: 600,
            border: "2px solid " + (geo.a.errors > 0 ? "#4a3030" : armed === auto ? GREEN : BLUE),
            background: armed === auto ? GREEN : "rgba(0,0,0,.25)",
            color: armed === auto ? "#0d1014" : geo.a.errors > 0 ? "#6b7280" : "#e6e9ef",
            cursor: geo.a.errors > 0 ? "not-allowed" : "pointer",
          }}
          onClick={() => armIt(auto)}>
          {armed === auto ? "ARMED" : geo.a.errors > 0 ? "cannot arm — has errors" : "Arm for match"}
        </button>
        <div style="margin-top:.5rem;max-height:22rem;overflow-y:auto">
          {geo.a.issues.length === 0
            ? <div style={{ color: GREEN, fontSize: ".8rem" }}>no problems</div>
            : geo.a.issues.map((i: any, k: number) => (
              <div key={k} style={{
                fontSize: ".74rem", padding: ".25rem .4rem", margin: ".25rem 0",
                borderLeft: "3px solid " + (i.lv === "err" ? RED : "#ffd166"),
                background: "rgba(0,0,0,.2)",
              }}>{i.t}</div>
            ))}
        </div>
      </div>
    </div>
  );

  /* ================================================================= MAP */
  const pathsOf = (n: string) => data.paths[n]?.autos || [];
  const mapView = (
    <div style="display:flex;gap:.6rem;align-items:flex-start">
      <div style="flex:1;min-width:0;max-height:34rem;overflow-y:auto">
        <div style={{ color: DIM, fontSize: ".7rem", textTransform: "uppercase", letterSpacing: ".08em", marginBottom: ".2rem" }}>
          Paths — click to see which autos use it
        </div>
        {pathNames.filter((n) => !q || n.toLowerCase().includes(q)).map((n) => {
          const d = data.paths[n];
          const lit = !!auto && !selPath && data.autos[auto].paths.includes(n);
          const dim = (!!selPath && selPath !== n) || (!selPath && !!auto && !lit);
          return (
            <div key={n} style={rowStyle(selPath === n, lit, dim)}
              onClick={() => setSelPath(selPath === n ? "" : n)}>
              <span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{n}</span>
              <span style={{ fontSize: ".68rem", color: d.shared ? "#ffd166" : DIM, fontWeight: d.shared ? 700 : 400 }}>
                {d.orphan ? "orphan" : d.autos.length + (d.autos.length > 1 ? " autos" : " auto")}
              </span>
            </div>
          );
        })}
      </div>

      <div style="flex:1;min-width:0;max-height:34rem;overflow-y:auto">
        <div style={{ color: DIM, fontSize: ".7rem", textTransform: "uppercase", letterSpacing: ".08em", marginBottom: ".2rem" }}>
          Autos — click to select, double-click to arm
        </div>
        {autoNames.filter((n) => !q || n.toLowerCase().includes(q)).map((n) => {
          const d = data.autos[n];
          const lit = !!selPath && pathsOf(selPath).includes(n);
          const dim = !!selPath && !lit;
          return (
            <div key={n} style={rowStyle(n === auto && !selPath, lit, dim)}
              onClick={() => { setAuto(n); setSelPath(""); }}
              onDblClick={() => armIt(n)}>
              <span style={{
                width: "6px", height: "6px", borderRadius: "50%", flex: "none",
                background: d.errors ? RED : d.warns ? "#ffd166" : GREEN,
              }} />
              <span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{n}</span>
              {armed === n && <span style={{ fontSize: ".65rem", color: GREEN }}>ARMED</span>}
              <span style={{ fontSize: ".68rem", color: d.errors ? RED : DIM }}>
                {d.errors ? d.errors + " err" : d.paths.length + " paths"}
              </span>
            </div>
          );
        })}
      </div>

      <div style="flex:0 0 16rem">
        {selPath ? (
          <Fragment>
            <div style={{ fontWeight: 600 }}>{selPath}</div>
            <div style={{ color: DIM, fontSize: ".75rem" }}>{data.paths[selPath].wps.length} waypoints</div>
            <div style={{ color: DIM, fontSize: ".65rem", textTransform: "uppercase", marginTop: ".5rem" }}>Markers</div>
            <div>{data.paths[selPath].mk.length === 0
              ? <span style={{ color: DIM, fontSize: ".78rem" }}>none</span>
              : data.paths[selPath].mk.map((m: any, i: number) => (
                <span key={i} style={{
                  display: "inline-block", fontSize: ".7rem", padding: "1px 5px", margin: "2px 3px 0 0",
                  borderRadius: "4px", border: "1px solid " + (m.emb ? RED : /Intake/.test(m.n) ? GREEN : ORANGE),
                  color: m.emb ? RED : /Intake/.test(m.n) ? GREEN : ORANGE,
                }}>{m.n}@{m.p.toFixed(2)}{m.emb ? " ⚠" : ""}</span>
              ))}</div>
            <div style={{ color: DIM, fontSize: ".65rem", textTransform: "uppercase", marginTop: ".5rem" }}>
              Used by {data.paths[selPath].autos.length}
            </div>
            {data.paths[selPath].autos.map((u: string) => (
              <div key={u} style={{ fontSize: ".78rem", color: BLUE, cursor: "pointer" }}
                onClick={() => { setAuto(u); setSelPath(""); setSub("field"); }}>{u}</div>
            ))}
            {data.paths[selPath].shared && (
              <div style={{
                marginTop: ".5rem", padding: ".4rem .5rem", fontSize: ".74rem",
                background: "rgba(90,60,20,.35)", border: "1px solid #5c421f", borderRadius: "6px",
              }}>
                <b style={{ color: "#ffd166" }}>Shared path.</b> Moving a marker here changes
                all {data.paths[selPath].autos.length} autos above.
              </div>
            )}
          </Fragment>
        ) : (
          <Fragment>
            <div style={{ fontWeight: 600 }}>{auto}</div>
            <div style={{ color: DIM, fontSize: ".75rem", marginBottom: ".3rem" }}>
              {geo.a.segs.length} paths · {geo.a.shots} shots
            </div>
            {geo.a.paths.map((p: string, i: number) => (
              <div key={i} style={{ fontSize: ".78rem" }}>
                <span style={{ color: DIM }}>{i + 1}. </span>
                <span style={{ color: BLUE, cursor: "pointer" }} onClick={() => setSelPath(p)}>{p}</span>
                {data.paths[p]?.shared && <span style={{ color: "#ffd166", fontSize: ".65rem" }}> shared</span>}
              </div>
            ))}
          </Fragment>
        )}
      </div>
    </div>
  );

  /* =============================================================== SHELL */
  const nErr = autoNames.filter((n) => data.autos[n].errors > 0).length;
  const nShared = pathNames.filter((n) => data.paths[n].shared).length;

  return (
    <div style="padding:.5rem .7rem;width:100%;text-align:left">
      <div style="display:flex;gap:.6rem;align-items:center;flex-wrap:wrap;margin-bottom:.5rem">
        {subTab("field", "Visualizer")}
        {subTab("map", "Map")}
        <input type="search" value={query} placeholder={sub === "field" ? "filter autos…" : "filter paths & autos…"}
          onInput={(e: any) => setQuery(e.currentTarget.value)}
          style={{
            background: "rgba(0,0,0,.25)", color: "#e6e9ef", border: "1px solid #2d333d",
            borderRadius: "6px", padding: ".2rem .6rem", minWidth: "12rem", fontSize: ".85rem",
          }} />
        <span style={{ color: DIM, fontSize: ".78rem" }}>
          {pathNames.length} paths · {autoNames.length} autos ·{" "}
          <span style={{ color: "#ffd166" }}>{nShared} shared</span> ·{" "}
          <span style={{ color: nErr ? RED : GREEN }}>{nErr} unloadable</span>
        </span>
        <button type="button" onClick={() => socket.send(id, "rescan")}
          style={{ background: "#232a35", color: "#e6e9ef", border: "1px solid #2d333d", borderRadius: "6px", padding: ".2rem .6rem", fontSize: ".8rem" }}>
          Rescan
        </button>
        {flash && <span style={{ color: flash.startsWith("REFUSED") || flash.startsWith("cannot") ? RED : GREEN, fontSize: ".8rem" }}>{flash}</span>}
      </div>

      {/* what is actually going to run at the start of the match */}
      <div style={{
        display: "flex", alignItems: "center", gap: ".6rem", marginBottom: ".5rem",
        padding: ".3rem .7rem", borderRadius: "8px",
        background: armed ? "rgba(116,255,6,.08)" : "rgba(0,0,0,.25)",
        border: "2px solid " + (armed ? GREEN : "#2d333d"),
      }}>
        <span style={{ fontSize: ".65rem", letterSpacing: ".1em", textTransform: "uppercase", color: DIM }}>
          Armed for match
        </span>
        <span style={{ fontSize: "1.05rem", fontWeight: 700, color: armed ? GREEN : "#6b7280" }}>
          {armed || "— none —"}
        </span>
        {armed && data.autos[armed] && (
          <span style={{ color: DIM, fontSize: ".78rem" }}>
            {data.autos[armed].segs.length} paths · {data.autos[armed].shots} shots
            {data.autos[armed].warns > 0 && (
              <span style={{ color: "#ffd166" }}> · {data.autos[armed].warns} warning(s)</span>
            )}
          </span>
        )}
        {armed && armed !== auto && (
          <button type="button" onClick={() => setAuto(armed)}
            style={{ background: "transparent", color: BLUE, border: "none", cursor: "pointer", fontSize: ".78rem" }}>
            show it
          </button>
        )}
      </div>

      {sub === "field" ? fieldView : mapView}
    </div>
  );
}
