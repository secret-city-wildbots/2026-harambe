import { h } from "preact";

interface BooleanReadoutProps {
    text: string;
    chars?: number;
    onColor?: string;
    offColor?: string;
    on?: boolean;
}

const BooleanReadout = ({
    text,
    chars,
    onColor = "rgba(116, 255, 6, 0.8)",
    offColor = "rgba(200, 200, 200, 0.8)",
    on = false,
}:BooleanReadoutProps) => {
    return (
        <b class="boolean-readout" style={{
            backgroundColor: (on) ? onColor:offColor,
            width: (typeof chars === "number") ? "0":"",
            overflow: "hidden",
            textAlign: "center"
        }}>
            {text}
        </b>
    )
}

export default BooleanReadout;