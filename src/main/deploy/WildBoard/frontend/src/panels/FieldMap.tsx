import { h } from "preact";
import { useEffect, useRef } from "preact/hooks";
import { WsEventBus } from "../ws/WSEventBus";

interface Props {
    socket: WsEventBus;
    id: number;
    alliance: boolean;
}

export default function FieldMap({ socket, id, alliance }: Props) {

    const containerRef = useRef<HTMLDivElement>(null);

    const fieldCanvasRef = useRef<HTMLCanvasElement>(null);
    const trailCanvasRef = useRef<HTMLCanvasElement>(null);
    const robotCanvasRef = useRef<HTMLCanvasElement>(null);

    const imgRef = useRef<HTMLImageElement>(null);

    const FIELD_W = 8.02;
    const FIELD_H = 16.54;
    const ROBOT_SIZE = 0.9;

    const crop = { left: 20, right: 20, top: 68, bottom: 62 };

    const targetPose = useRef({ x: 0, y: 0, theta: 0 });
    const currentPose = useRef({ x: 0, y: 0, theta: 0 });

    const trail = useRef<{ x: number; y: number }[]>([]);
    const layout = useRef({ scale: 1, offsetX: 0, offsetY: 0 });

    const lastTime = useRef(performance.now());

    const MAX_TRAIL = 100;

    function computeLayout() {

        const container = containerRef.current!;
        const img = imgRef.current!;

        const rect = container.getBoundingClientRect();

        const canvases = [
            fieldCanvasRef.current!,
            trailCanvasRef.current!,
            robotCanvasRef.current!
        ];

        canvases.forEach(c => {
            c.width = rect.width;
            c.height = rect.height;
        });

        const scaleX = rect.width / img.naturalWidth;
        const scaleY = rect.height / img.naturalHeight;

        const scaledCrop = {
            left: crop.left * scaleX,
            right: crop.right * scaleX,
            top: crop.top * scaleY,
            bottom: crop.bottom * scaleY,
        };

        const usableWidth = rect.width - scaledCrop.left - scaledCrop.right;
        const usableHeight = rect.height - scaledCrop.top - scaledCrop.bottom;

        const scale = Math.min(usableWidth / FIELD_W, usableHeight / FIELD_H);

        const offsetX = scaledCrop.left + (usableWidth - FIELD_W * scale) / 2;
        const offsetY = scaledCrop.top + (usableHeight - FIELD_H * scale) / 2;

        layout.current = { scale, offsetX, offsetY };

        drawField();
        redrawTrail();
    }

    function drawField() {

        const canvas = fieldCanvasRef.current!;
        const ctx = canvas.getContext("2d")!;
        const img = imgRef.current!;

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
    }

    function redrawTrail() {

        const canvas = trailCanvasRef.current!;
        const ctx = canvas.getContext("2d")!;

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        if (trail.current.length === 0) return;

        const { scale, offsetX, offsetY } = layout.current;

        ctx.strokeStyle = "rgba(0,255,0,0.5)";
        ctx.lineWidth = 2;

        ctx.beginPath();

        trail.current.forEach((p, i) => {

            const px = offsetX + p.x * scale;
            const py = offsetY + FIELD_H * scale - p.y * scale;

            if (i === 0) ctx.moveTo(px, py);
            else ctx.lineTo(px, py);

        });

        ctx.stroke();
    }

    function drawRobot(now: number) {

        const dt = (now - lastTime.current) / 1000;
        lastTime.current = now;

        const canvas = robotCanvasRef.current!;
        const ctx = canvas.getContext("2d")!;

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        const { scale, offsetX, offsetY } = layout.current;

        const pose = currentPose.current;
        const target = targetPose.current;

        const lambda = 8;
        const alpha = 1 - Math.exp(-lambda * dt);

        pose.x += (target.x - pose.x) * alpha;
        pose.y += (target.y - pose.y) * alpha;
        pose.theta += (target.theta - pose.theta) * alpha;

        const last = trail.current[trail.current.length - 1];

        if (!last || Math.hypot(pose.x - last.x, pose.y - last.y) > 0.01) {

            trail.current.push({ x: pose.x, y: pose.y });

            if (trail.current.length > MAX_TRAIL)
                trail.current.shift();

            redrawTrail();
        }

        const px = offsetX + pose.x * scale;
        const py = offsetY + FIELD_H * scale - pose.y * scale;

        const size = ROBOT_SIZE * scale;

        ctx.save();

        ctx.translate(px, py);
        ctx.rotate((-pose.theta * Math.PI) / 180);

        ctx.fillStyle = "#00ff00";
        ctx.strokeStyle = "#fff";
        ctx.lineWidth = 2;

        ctx.fillRect(-size / 2, -size / 2, size, size);
        ctx.strokeRect(-size / 2, -size / 2, size, size);

        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(size / 2 + 10, 0);
        ctx.stroke();

        ctx.restore();
    }

    useEffect(() => {

        const container = containerRef.current!;
        const img = imgRef.current!;

        const resizeObserver = new ResizeObserver(computeLayout);

        resizeObserver.observe(container);

        img.onload = computeLayout;

        return () => resizeObserver.disconnect();

    }, []);

    useEffect(() => {

        const unsubscribe = socket.subscribe(id, (updated: string) => {

            const parts = updated.trim().split(",");

            if (parts.length >= 3) {

                targetPose.current = {
                    x: parseFloat(parts[0]),
                    y: parseFloat(parts[1]),
                    theta: parseFloat(parts[2]),
                };

            }

        });

        return () => unsubscribe();

    }, [socket, id]);

    useEffect(() => {

        let raf: number;

        const frame = (t: number) => {
            drawRobot(t);
            raf = requestAnimationFrame(frame);
        };

        raf = requestAnimationFrame(frame);

        return () => cancelAnimationFrame(raf);

    }, []);

    return (
        <div style="width:100%;height:100%;display:flex;flex-direction:column;align-items:center;min-height:0;">
            <label>Field</label>

            <div ref={containerRef} class="field-map">

                <img
                    ref={imgRef}
                    src={"/assets/fieldmap" + (alliance ? "red" : "blue") + ".png"}
                    style="display:none"
                />

                <canvas ref={fieldCanvasRef} class="field-map-canvas"/>
                <canvas ref={trailCanvasRef} class="field-map-canvas"/>
                <canvas ref={robotCanvasRef} class="field-map-canvas"/>

            </div>
        </div>
    );
}