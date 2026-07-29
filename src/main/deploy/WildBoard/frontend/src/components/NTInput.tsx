import { h } from "preact";
import { useState, useEffect } from "preact/hooks";
import Input from "./Input.tsx";
import { Socket } from "socket.io-client";

interface NTInputProps {
    nt: string;
    socket: Socket;
    defaultVal?: number|string;
    number?: boolean
    chars?: number;
    precision?: number;
    color?: string;
    small?: boolean;
}

const NTInput = ({
    nt,
    socket,
    defaultVal,
    number = false,
    chars,
    precision,
    color,
    small
}: NTInputProps) => {
    if (defaultVal == undefined || defaultVal == null) {
        if (number) defaultVal = 0;
        else defaultVal = ""
    }

    socket.emit(nt, defaultVal);

    const setValueWrapper = (newVal: string|number) => {
        socket.emit(nt, newVal);
    };

    return <Input number={number} chars={chars} color={color} small={small} precision={precision} defaultVal={defaultVal} valSetter={setValueWrapper} />;
};

export default NTInput;
