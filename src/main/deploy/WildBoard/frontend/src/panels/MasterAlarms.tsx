import { h } from "preact";
import { useEffect, useState, useRef } from "preact/hooks";
import { WsEventBus } from "../ws/WSEventBus";
import Button from "../components/Button";

interface Props {
    socket: WsEventBus;
    id: number;

    texts: string[];
    cols?: number;
    height?: number;
    descriptions?: string[]; // optional descriptions for popups
}

export default function ({
    texts,
    socket,
    id,
    cols = 2,
    height = 20,
    descriptions = []
}: Props) {
    const [highlighted, setHighlighted] = useState<boolean[]>(
        Array(texts.length).fill(false)
    );

    const rows = Math.ceil(texts.length / cols);
    const squareRefs = useRef<Array<HTMLDivElement | null>>([]);
    const pingAlarm = texts.findIndex((val: string) => (val.toLowerCase().includes("ping")));
    const fpsAlarm = texts.findIndex((val: string) => (val.toLowerCase().includes("fps")));

    // Subscribe to WS updates
    useEffect(() => {
        const unsubscribe = socket.subscribe(id, (updated: string) => {
            console.log(updated);
            setHighlighted(
                updated.split("").map((s) => s.trim() === "1")
            );
        });

        const unsubPing = socket.subscribePing((ping: number) => {
            if (ping > 1000) {
                let modified = highlighted.slice();
                modified[pingAlarm] = true;
                setHighlighted(modified);
            }
        });

        const unsubFPS = socket.subscribePing((fps: number) => {
            if (fps < 2) {
                let modified = highlighted.slice();
                modified[fpsAlarm] = true;
                setHighlighted(modified);
            }
        });

        return () => {unsubscribe();unsubPing()}
    }, [socket, id]);

    // Initialize Bootstrap popovers on render
    useEffect(() => {
        squareRefs.current.forEach((el) => {
            if (el && (window as any).bootstrap) {
                const existing = (window as any).bootstrap.Popover.getInstance(el);
                if (existing) existing.dispose();

                new (window as any).bootstrap.Popover(el, {
                    trigger: "focus", // click/focus triggers popup
                    placement: "top",
                    html: true
                });
            }
        });
    }, [texts, descriptions]);

    return (
        <div style="flex: 1; display: flex; justify-content: center; align-items: center; flex-direction: column; height: 100%;">
            <div
                class="grid-squares"
                style={{
                    height: `${height}rem`,
                    gridTemplateColumns: `repeat(${cols}, minmax(0, 1fr))`,
                    gridTemplateRows: `repeat(${rows}, 1fr)`,
                    marginBottom: "0.5rem",
                }}
            >
                {texts.map((text, i) => (
                    <div
                        key={i}
                        ref={(el) => {
                            squareRefs.current[i] = el; // assign but return nothing
                        }}
                        class={`grid-square ${highlighted[i] ? "highlighted" : ""}`}
                        tabIndex={0} // required for focus trigger
                        data-bs-toggle="popover"
                        data-bs-content={descriptions[i] || ""}
                    >
                        {text}
                    </div>
                ))}
            </div>
            <Button text="Clear Alarms" onClick={() => socket.send(id, "c")} />
        </div>
    );
}