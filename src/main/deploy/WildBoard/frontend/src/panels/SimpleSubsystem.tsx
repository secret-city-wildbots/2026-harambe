import { h, Fragment } from "preact";
import ArmableButton from "../components/ArmableButton.tsx";
import Switch from "../components/Switch.tsx";
import { WsEventBus } from "../ws/WSEventBus.ts";
import Readout from "../components/Readout.tsx";
import { useState, useEffect } from "preact/hooks";
import FlexRow from "../components/FlexRow.tsx";

interface Props {
    absolute?: boolean;
    unit?: string;
    fillContainer?: boolean;
    velocity?: boolean;
    precision?: number;
    name: string;

    id: number;
    socket: WsEventBus;
}

export default function ({
    absolute = false,
    unit,
    fillContainer = false,
    velocity = false,
    precision,
    name,
    id,
    socket,
}: Props) {
    const [pos, setPos] = useState("_");
    const [temp, setTemp] = useState("_");

    if (!unit) {
        unit = velocity ? "rps" : "deg";
    }

    if (precision === undefined) {
        precision = velocity ? 1 : 0;
    }

    useEffect(() => {
        const unsubscribe = socket.subscribe(id, (data: string) => {
            const index = data.indexOf(",");
            if (index < 0) return;

            let posVal = data.substring(0, index);
            let tempVal = data.substring(index + 1);

            setPos(Number(posVal).toFixed(precision));
            setTemp(Number(tempVal).toFixed(1));
        });

        return () => unsubscribe();
    }, [socket, id, precision]);

    const onToggle = (on: boolean) => {
        socket.send(id, on ? "ul" : "l");
    };

    const onClick = () => {
        //start calibrating
        socket.send(id, "c1");
    };

    const onRelease = () => {
        //stop calibrating
        socket.send(id, "c0");
    };

    return (
        <div
            class={fillContainer ? " h-100":""}
            style={{ //vertically align in column
                display: "flex",
                justifyContent: "center",
                flexDirection: "column",
                padding: (fillContainer ? "0.5rem" : "1rem"),
                paddingTop: "0.25rem",
                paddingBottom: "0.25rem",
            }}
        >
            <label>{name}</label>
            {!velocity && !absolute ? (
                <>
                    <ArmableButton
                        text="ZERO"
                        onToggle={onToggle}
                        onClick={onClick}
                        onRelease={onRelease}
                    />
                    {/*we need to detect chrome, as it likes to interpret the <br> as being huge, which looks bad.*/}
                    {!(typeof InstallTrigger !== 'undefined') ? (<></>) : (<br />)}
                </>
            ) : (
                <></>
            )}
            {absolute ? (
                <div>
                    <FlexRow noPadding>
                        <label class="label-small pr-2">Unlock</label>
                        <Switch onColor="#ef0001" offColor="rgba(116, 255, 6, 1)" onToggle={onToggle} />
                    </FlexRow>
                    <div>
                        <div style="min-height: 2.5rem; margin-top: 1rem;" class="flex-row">
                            <label class="label-small pr-3">
                                {velocity ? "Vel" : "Pos"} ({unit})
                            </label>
                            <Readout text={pos} chars={4} />
                        </div>
                        <div style="min-height: 2.5rem" class="flex-row">
                            <label class="label-small pr-3">Temp (C)</label>
                            <Readout text={temp} chart temperature chars={4} />
                        </div>
                    </div>
                </div>
            ) : (
                <>
                    <div style="min-height: 2.5rem; margin-top: 0.5rem;" class="flex-row">
                        <label class="label-small pr-2">
                            {velocity ? "Vel" : "Pos"} ({unit})
                        </label>
                        <Readout text={pos} chars={4} />
                    </div>
                    <div style="min-height: 2.5rem" class="flex-row">
                        <label class="label-small pr-3">Temp (C)</label>
                        <Readout text={temp} chart temperature chars={4} />
                    </div>
                </>
            )}
        </div>
    );
};