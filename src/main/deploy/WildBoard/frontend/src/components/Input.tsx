import { h } from "preact";
import { useEffect, useRef, useState } from "preact/hooks";
import Button from "./Button.tsx";

interface InputProps {
    valSetter?: (val: string | number) => void;
    defaultVal?: string | number;
    chars?: number;
    color?: string;
    small?: boolean;
    noSendBtn?: boolean;
    number?: boolean;
    precision?: number;
    arrowIncrement?: number;
    max?: number;
    min?: number;
}

const Input = ({
    valSetter = (val: string | number) => {},
    defaultVal,
    chars = 5,
    small = false,
    noSendBtn = false,
    number = false,
    color = "#eed",
    precision,
    arrowIncrement = 0.1,
    max,
    min,
}: InputProps) => {
    //set defaultVal default according to input type
    if (typeof defaultVal == undefined || typeof defaultVal == null) {
        if (number) defaultVal = 0;
        else defaultVal = "";
    }

    const [shiftPressed, setShiftPressed] = useState(false);
    const [ctrlPressed, setCtrlPressed] = useState(false);
    const [currentVal, setCurrentVal] = useState(defaultVal);
    const inputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        const onKey = (e: KeyboardEvent) => {
            if (!noSendBtn) {
                setShiftPressed(e.shiftKey);
            }
            setCtrlPressed(e.ctrlKey);
        };
        document.addEventListener("keydown", onKey);
        document.addEventListener("keyup", onKey);
        return () => {
            document.removeEventListener("keydown", onKey);
            document.removeEventListener("keyup", onKey);
        };
    }, []);

    const onUpdate = () => {
        if (inputRef.current) {
            let newVal: string | number = inputRef.current.value;

            if (number) {
                newVal = parseFloat(newVal);
                if (isNaN(newVal)) newVal = 0;

                if (precision !== undefined) {
                    if (precision == 0) {
                        newVal = Math.floor(newVal);
                    } else {
                        newVal = newVal.toFixed(precision);

                        newVal = parseFloat(newVal);
                        if (isNaN(newVal)) newVal = 0;
                    }
                }
            }

            valSetter(newVal);
            setCurrentVal(newVal);
        }
    };

    const increment = () => {
        if (!inputRef.current) return;
        let v = parseFloat(inputRef.current.value);
        if (isNaN(v)) v = 0;
        inputRef.current.value = (v + arrowIncrement * (ctrlPressed ? 10 : 1)).toString();
        onUpdate();
    };

    const decrement = () => {
        if (!inputRef.current) return;
        let v = parseFloat(inputRef.current.value);
        if (isNaN(v)) v = 0;
        inputRef.current.value = (v - arrowIncrement * (ctrlPressed ? 10 : 1)).toString();
        onUpdate();
    };

    return (
        <div
            style={{
                position: "relative", // make this the containing block
                display: "grid",
                gridTemplateColumns: shiftPressed ? "1fr auto" : "1fr",
                alignItems: "center",
                width: `calc(${chars}ch + 3rem)`,
                maxWidth: "100%",
                gap: 0,
            }}
        >
            <input
                type={number ? "number" : "text"}
                ref={inputRef}
                value={currentVal}
                onInput={onUpdate}
                class={"readout" + (small ? " small" : "")}
                style={{
                    color,
                    width: "100%",
                    minWidth: 0,
                    paddingRight: number && !shiftPressed ? "1.9rem" : "0.5rem",
                    borderTopRightRadius: shiftPressed ? 0 : undefined,
                    borderBottomRightRadius: shiftPressed ? 0 : undefined,
                }}
            />

            {/* Shift‑save button (still inside grid flow) */}
            {shiftPressed && (
                <Button
                    text={
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="16"
                            height="16"
                            fill="currentColor"
                            viewBox="0 0 16 16"
                        >
                            <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5" />
                            <path d="M7.646 1.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1-.708.708L8.5 2.707V11.5a.5.5 0 0 1-1 0V2.707L5.354 4.854a.5.5 0 1 1-.708-.708z" />
                        </svg>
                    }
                    onClick={onUpdate}
                    style={{
                        borderTopLeftRadius: 0,
                        borderBottomLeftRadius: 0,
                        height: "100%",
                        padding: "0.3rem 0.5rem",
                    }}
                />
            )}

            {/* Ctrl arrows (absolutely positioned inside the same wrapper) */}
            {number && !shiftPressed && (
                <div
                    style={{
                        position: "absolute",
                        right: `${ctrlPressed ? 0.3 : 0.5}rem`, // tweak to sit inside padding
                        top: "50%",
                        transform: "translateY(-50%)",
                        display: "flex",
                        flexDirection: "column",
                        gap: "0.1rem",
                        pointerEvents: "none", // let clicks through to the Buttons
                    }}
                >
                    <Button
                        text="▲"
                        onClick={increment}
                        style={{
                            pointerEvents: "auto", // re-enable clicks on the arrows
                            padding: `0 ${ctrlPressed ? 0.4 : 0.3}rem`,
                            fontSize: `${ctrlPressed ? 1 : 0.8}rem`,
                            lineHeight: `${ctrlPressed ? 1 : 0.8}rem`,
                            borderRadius: `${ctrlPressed ? 10 : 6}px`,
                        }}
                    />
                    <Button
                        text="▼"
                        onClick={decrement}
                        style={{
                            pointerEvents: "auto",
                            padding: `0 ${ctrlPressed ? 0.4 : 0.3}rem`,
                            fontSize: `${ctrlPressed ? 1 : 0.8}rem`,
                            lineHeight: `${ctrlPressed ? 1 : 0.8}rem`,
                            borderRadius: `${ctrlPressed ? 10 : 6}px`,
                        }}
                    />
                </div>
            )}
        </div>
    );
};

export default Input;
