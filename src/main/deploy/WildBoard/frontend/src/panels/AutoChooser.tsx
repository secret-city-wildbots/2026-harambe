import { h } from "preact";
import { useState, useEffect } from "preact/hooks";
import { WsEventBus } from "../ws/WSEventBus";

/**
 * Shows which auto is armed. Display only — autos are picked in the Autos tab,
 * which refuses to arm anything that would fail to load.
 *
 * Payload from Java is "<name>|<state>", state one of ok | warn | none.
 */

interface Props {
  socket: WsEventBus;
  id: number;
}

export default function ({ socket, id }: Props) {
  const [name, setName] = useState("");
  const [state, setState] = useState("none");

  useEffect(() => {
    const off = socket.subscribe(id, (msg: string) => {
      const cut = msg.lastIndexOf("|");
      if (cut < 0) return;
      setName(msg.slice(0, cut));
      setState(msg.slice(cut + 1));
    });
    // Ask what is armed. The robot may already have one from before this page
    // loaded, and Java only pushes on change.
    socket.send(id, "hello");
    const retry = setTimeout(() => socket.send(id, "hello"), 1200);
    return () => { off(); clearTimeout(retry); };
  }, [socket, id]);

  const armed = state !== "none" && name.length > 0;
  const colour = state === "warn" ? "#ffd166" : armed ? "rgba(116,255,6,0.85)" : "#6b7280";

  return (
    <div style="padding: 0.5rem; width: 100%;">
      <label class="label-small" style="margin: 0; padding: 0; display: block; text-align: left;">
        Auto
      </label>
      <div
        class="readout"
        style={{
          display: "block",
          width: "100%",
          color: colour,
          borderColor: colour,
          fontSize: name.length > 18 ? "0.95rem" : "1.2rem",
          lineHeight: "1.35rem",
          textAlign: "center",
          wordBreak: "break-word",
          marginTop: "0.2rem",
        }}
        title={armed ? name : "nothing armed — pick one in the Autos tab"}
      >
        {armed ? name : "— none —"}
      </div>
      {state === "warn" && (
        <div style="font-size: 0.7rem; color: #ffd166; text-align: center; margin-top: 0.15rem">
          has warnings
        </div>
      )}
    </div>
  );
}
