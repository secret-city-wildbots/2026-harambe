import { h } from "preact";
import { useState, useEffect } from "preact/hooks";
import BooleanReadout from "./BooleanReadout.tsx";
import { Socket } from "socket.io-client";

interface NTBooleanReadoutProps {
    nt: string;
    socket: Socket;
    text: string;
    defaultVal?: boolean;
    index?: number;
    chars?: number;
    onColor?: string;
    offColor?: string;
}

const NTBooleanReadout = ({
    nt,
    socket,
    text,
    defaultVal = false,
    index,
    chars,
    onColor,
    offColor,
}:NTBooleanReadoutProps) => {
    const [value, setValue] = useState(defaultVal);

    useEffect(() => {
        const handler = (newVal:boolean) => {
            if (newVal !== undefined && newVal !== null) {
                if (Array.isArray(newVal) && typeof index === "number") {
                    newVal = newVal[index];
                }

                setValue(newVal);
            }
        }

        socket.on(nt, handler);

        // Cleanup listener on unmount or if nt/socket changes
        return () => {
        socket.off(nt, handler);
        };
    }, [nt, socket, index])

    return (
        <BooleanReadout text={text} chars={chars} onColor={onColor} offColor={offColor} on={value} />
    )
}

export default NTBooleanReadout;