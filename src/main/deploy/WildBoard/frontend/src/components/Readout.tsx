import { h, Fragment } from 'preact';
import AngleDisplay from "./AngleDisplay.tsx";
import { useEffect, useRef, useState } from 'preact/hooks';
import Chart from 'chart.js/auto';

interface ReadoutProps {
  text?: string | number;
  chars?: number;
  color?: string;
  small?: boolean;
  angle?: boolean;
  temperature?: boolean;
  chart?: boolean;
  min?: number;
  max?: number;
  lineColor?: string;
}

const MAX_POINTS = 50*3;

// temperature color keyframes (thresholds descending)
const TEMP_COLOR_KEYFRAMES: Array<{ threshold: number; color: string }> = [
  { threshold: 60, color: 'red' },
  { threshold: 50, color: 'yellow' },
  { threshold: -Infinity, color: 'rgb(68,142,205)' },
];

const Readout = ({
  text,
  chars,
  angle = false,
  small = false,
  color = '#eed',
  temperature = false,
  chart = false,
  min,
  max,
  lineColor,
}: ReadoutProps) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const chartRef = useRef<any | null>(null);
  const dataRef = useRef<number[]>([]);
  const [initialized, setInitialized] = useState(false);

  if (temperature && chart) {
    if (min === undefined) min = 0;
    if (max === undefined) max = 90;
  }

  if (!lineColor && chart && !temperature) {
    lineColor = 'rgb(68,142,205)';
  }
  // If chart is not requested, render the original simple readout to avoid layout changes
  if (!chart) {
    return (
      <div style="margin-bottom: 0; padding-bottom: 0;">
        <div
          style={
            {
              ...(angle
                ? {
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    flexDirection: 'row',
                    color: color,
                    width: chars ? `${chars * 1.3}rem` : undefined,
                    fontSize: '1.2rem',
                    lineHeight: '1.2rem',
                    // set CSS vars for padding so chart overlay can align to the inner area
                    ['--rpad-y' as any]: '0.2rem',
                    ['--rpad-x' as any]: '0.5rem',
                  }
                : {
                    display: 'inline-block',
                    color: color,
                    width: chars ? `${chars * 1.3}rem` : undefined,
                    ['--rpad-y' as any]: '0.3rem',
                    ['--rpad-x' as any]: '0.5rem',
                  }),
              ...(temperature
                ? Number(text) > 60
                  ? { borderColor: 'red' }
                  : Number(text) > 50
                  ? { borderColor: 'yellow' }
                  : {}
                : {}),
            }
          }
          class={"readout" + (small ? ' small' : '')}
        >
          {angle ? (
            <>
              <div style="padding-right: 0rem;">{(angle ? text?.toString().padStart(3, '0') : text)}</div>
              <div><AngleDisplay angle={Number(text)} radius={25} /></div>
            </>
          ) : (
            <>{text}</>
          )}
        </div>
      </div>
    );
  }

  // helper to pick a temperature color from keyframes
  function pickTempColor(val: number): string {
    for (const kf of TEMP_COLOR_KEYFRAMES) {
      if (val >= kf.threshold) return kf.color;
    }
    return TEMP_COLOR_KEYFRAMES[TEMP_COLOR_KEYFRAMES.length - 1].color;
  }

  // update data buffer when text changes
  useEffect(() => {
    const v = Number(text);
    const isNum = !Number.isNaN(v) && text !== undefined && text !== null && text !== '_';

    if (isNum) {
      dataRef.current.push(v);
    } else {
      if (dataRef.current.length === 0) dataRef.current.push(0);
      else dataRef.current.push(dataRef.current[dataRef.current.length - 1]);
    }

    if (dataRef.current.length > MAX_POINTS) dataRef.current.shift();

    if (chartRef.current && chartRef.current.data) {
      // update dataset values
      chartRef.current.data.labels = dataRef.current.map(() => '');
      chartRef.current.data.datasets[0].data = dataRef.current.slice();

      // dynamically update color if using temperature defaults (doesn't re-init chart)
      const currentLineColor = lineColor ?? (temperature ? pickTempColor(Number(text)) : color);
      let currentArea = 'rgba(255,255,255,0.06)';
      if (typeof currentLineColor === 'string' && currentLineColor.startsWith('#')) {
        currentArea = hexToRgba(currentLineColor, 0.06);
      } else if (typeof currentLineColor === 'string' && currentLineColor.startsWith('rgb')) {
        currentArea = currentLineColor.replace('rgb(', 'rgba(').replace(')', ',0.06)');
      }
      chartRef.current.data.datasets[0].borderColor = currentLineColor;
      chartRef.current.data.datasets[0].backgroundColor = currentArea;

      try {
        chartRef.current.update('none');
      } catch {}
    }
  }, [text, lineColor, color, temperature]);

  // create/destroy chart
  // compute initial actual line color (lineColor prop overrides; otherwise
  // temperature mode picks a heat-based color; fallback to text color)
  const initialLineColor = lineColor ?? (temperature ? pickTempColor(Number(text)) : color);
  // compute area color from actual line color
  let areaColor = 'rgba(255,255,255,0.06)';
  if (typeof initialLineColor === 'string' && initialLineColor.startsWith('#')) {
    areaColor = hexToRgba(initialLineColor, 0.06);
  } else if (typeof initialLineColor === 'string' && initialLineColor.startsWith('rgb')) {
    areaColor = initialLineColor.replace('rgb(', 'rgba(').replace(')', ',0.06)');
  }

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // initialize data array with current value
    if (dataRef.current.length === 0) {
      const v = Number(text);
      dataRef.current = [(!Number.isNaN(v) ? v : 0)];
    }
    // compute area color now that props are available
    let computedArea = areaColor;
    const actualLineColor = lineColor ?? (temperature ? pickTempColor(Number(text)) : color);

    // create chart on next frame so layout has settled and CSS sizes apply.
    // We use a small, simple responsive Chart.js setup and let Chart handle
    // DPR and buffer sizing; the canvas is placed inside .readout-plot which
    // is sized by CSS vars to match the readout padding.
    const raf = requestAnimationFrame(() => {
      try {
        // ensure the canvas takes full plot area
        canvas.style.left = '0';
        canvas.style.top = '0';
        canvas.style.width = '100%';
        canvas.style.height = '100%';
      } catch {}

      chartRef.current = new Chart(ctx, {
        type: 'line',
        data: {
          labels: dataRef.current.map(() => ''),
          datasets: [
            {
              data: dataRef.current.slice(),
              borderColor: actualLineColor,
              backgroundColor: computedArea,
              fill: true,
              pointRadius: 0,
              tension: 0.25,
            },
          ],
        },
        options: {
          animation: false,
          responsive: true,
          maintainAspectRatio: false,
          devicePixelRatio: window.devicePixelRatio || 1,
          layout: { padding: 0 },
          plugins: {
            legend: { display: false },
            tooltip: { enabled: false },
            /*decimation: {
              enabled: true,
              algorithm: 'min-max',
            },*/
          },
          scales: {
            x: { display: false },
            y: { display: false, min: min !== undefined ? min : 0, max: max !== undefined ? max : undefined },
          },
          elements: {
            line: { borderWidth: 1.5 },
          },
        },
      });
      // ensure Chart picks up the current devicePixelRatio and sizes its
      // internal buffer to match the CSS display size. Then observe size
      // changes so future resizes keep the buffer crisp.
      try {
        if (chartRef.current && chartRef.current.options) {
          // align chart DPR explicitly
          try { chartRef.current.options.devicePixelRatio = window.devicePixelRatio || 1; } catch {}
        }
        try { chartRef.current.resize(); } catch {}
      } catch {}

      // ResizeObserver to keep chart buffer matched to display size
      let ro: ResizeObserver | null = null;
      try {
        const plotEl = canvas.parentElement as HTMLElement | null;
        if (typeof ResizeObserver !== 'undefined' && plotEl) {
          ro = new ResizeObserver(() => {
            try {
              if (chartRef.current) {
                // ensure devicePixelRatio remains current (in case of zoom/DPR change)
                try { chartRef.current.options.devicePixelRatio = window.devicePixelRatio || 1; } catch {}
                chartRef.current.resize();
              }
            } catch {}
          });
          ro.observe(plotEl);
        } else {
          // fallback: window resize
          const h = () => { try { chartRef.current && chartRef.current.resize(); } catch {} };
          window.addEventListener('resize', h);
          // store handler on chartRef for cleanup
          (chartRef as any)._winHandler = h;
        }
      } catch {}
    });

    setInitialized(true);

    return () => {
      try {
        chartRef.current?.destroy();
      } catch {}
      chartRef.current = null;
      try {
        cancelAnimationFrame(raf);
      } catch {}

      // cleanup ResizeObserver or window resize fallback
      try {
        // disconnect ResizeObserver if present
        const canvasEl = canvasRef.current;
        if (canvasEl && (canvasEl.parentElement as any)) {
          const plotEl = canvasEl.parentElement as HTMLElement;
          // Try to stop any ResizeObserver observing plotEl
          // We don't keep a direct reference to ro here, so best-effort: if
          // there is a ResizeObserver on the element it will be GC'd on chart destroy.
        }
        // remove fallback window handler if set
        if ((chartRef as any)._winHandler) {
          try { window.removeEventListener('resize', (chartRef as any)._winHandler); } catch {}
          (chartRef as any)._winHandler = null;
        }
      } catch {}
    };
  }, [lineColor, color, temperature, min, max]); // re-init if visual props or bounds change

  // helper to convert hex color to rgba with alpha
  function hexToRgba(hex: string, alpha: number) {
    if (!hex) return `rgba(255,255,255,${alpha})`;
    let h = hex.replace('#', '');
    if (h.length === 3) {
      h = h.split('').map((c) => c + c).join('');
    }
    if (h.length !== 6) return `rgba(255,255,255,${alpha})`;
    const r = parseInt(h.slice(0, 2), 16);
    const g = parseInt(h.slice(2, 4), 16);
    const b = parseInt(h.slice(4, 6), 16);
    return `rgba(${r},${g},${b},${alpha})`;
  }

  // build base style used by the original readout so chart and non-chart
  // renderings look identical
  const baseStyle: any = {
    ...(angle
      ? {
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          flexDirection: 'row',
          color: color,
          width: chars ? `${chars * 1.3}rem` : undefined,
          fontSize: '1.2rem',
          lineHeight: '1.2rem',
          // expose padding via CSS variables so the overlay plot can match
          ['--rpad-y' as any]: '0.2rem',
          ['--rpad-x' as any]: '0.5rem',
        }
      : {
          display: 'inline-block',
          color: color,
          width: chars ? `${chars * 1.3}rem` : undefined,
        }),
    ...(temperature
      ? Number(text) > 60
        ? { borderColor: 'red' }
        : Number(text) > 50
        ? { borderColor: 'yellow' }
        : {}
      : {}),
  };

  // canvas sits behind the .readout box; we don't add another border so the
  // readout keeps its original look and sizing.
  const canvasStyle: any = {
    position: 'absolute',
    left: 0,
    top: 0,
    width: '100%',
    height: '100%',
    display: 'block',
    zIndex: 0,
    pointerEvents: 'none',
    opacity: initialized ? 1 : 0,
  };

  // prepare y axis bounds are read from props `min` and `max`

  // render chart-enabled readout
  return (
      <div style={{ marginBottom: 0, paddingBottom: 0 }}>
      <div
        style={{ ...baseStyle, position: 'relative', overflow: 'hidden' }}
        class={"readout" + (chart ? ' chart' : '') + (small ? ' small' : '')}
      >
        <div class="readout-plot">
          <canvas ref={canvasRef} />
        </div>

        <span style={{ position: 'relative', zIndex: 2, width: '100%' }}>
          {angle ? (
            <>
              <div style={{ paddingRight: '0rem' }}>{(angle ? text?.toString().padStart(3, '0') : text)}</div>
              <div><AngleDisplay angle={Number(text)} radius={25} /></div>
            </>
          ) : (
            <>{text}</>
          )}
        </span>
      </div>
    </div>
  );
};

export default Readout;