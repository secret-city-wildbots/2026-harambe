import { h, Fragment } from "preact";
import { useState } from "preact/hooks";

import { Socket } from "socket.io-client";
import Dropdown from "../components/Dropdown.tsx";
import NTInput from "../components/NTInput.tsx";
import Input from "../components/Input.tsx";
import FlexRow from "../components/FlexRow.tsx";
import Button from "../components/Button.tsx";

interface DriverProfilesProps {
    socket: Socket;
    dashboardItem?: boolean;
}

const DriverProfiles = ({ socket, dashboardItem = false }: DriverProfilesProps) => {
    const [driver, setDriver] = useState("Devin");
    const [driverNames, setDriverNames] = useState(["Devin"]);

    socket.on("Legal_Drivers", (val) => {
        setDriverNames(val);
        socket.emit("Selected_Driver", driver);
    });

    const setDriverVal = (newVal: string) => {
        setDriver(newVal);
        socket.emit("Selected_Driver", driver);
    };

    return (
        <div
            class={"driver-profiles" + (dashboardItem ? " column-item" : "")}
            style={{
                display: "flex",
                justifyContent: "center",
                flexDirection: "column",
                wrap: "no-wrap",
            }}
        >
            <FlexRow>
                <Dropdown items={driverNames} valSetter={setDriverVal} />
                <Button text="SAVE" />
            </FlexRow>
            <table>
                <tr>
                    <td>
                        <label class="label-small">Strafe Deadband</label>
                    </td>
                    <td>
                        <Input
                            number
                            noSendBtn
                            defaultVal={0.08}
                            arrowIncrement={0.01}
                            precision={2}
                            chars={5}
                        />
                    </td>
                </tr>
                <tr>
                    <td>
                        <label class="label-small">Strafe Scaling</label>
                    </td>
                    <td>
                        <Input
                            number
                            noSendBtn
                            defaultVal={3.0}
                            arrowIncrement={0.1}
                            precision={2}
                            chars={5}
                        />
                    </td>
                </tr>
                <tr>
                    <td>
                        <label class="label-small">Strafe Max</label>
                    </td>
                    <td>
                        <Input
                            number
                            noSendBtn
                            defaultVal={1.0}
                            arrowIncrement={0.01}
                            precision={2}
                            chars={5}
                        />
                    </td>
                </tr>
                <tr>
                    <td>
                        <label class="label-small">Rotate Deadband</label>
                    </td>
                    <td>
                        <Input
                            number
                            noSendBtn
                            defaultVal={0.15}
                            arrowIncrement={0.01}
                            precision={2}
                            chars={5}
                        />
                    </td>
                </tr>
                <tr>
                    <td>
                        <label class="label-small">Rotate Scaling</label>
                    </td>
                    <td>
                        <Input
                            number
                            noSendBtn
                            defaultVal={3.5}
                            arrowIncrement={0.1}
                            precision={2}
                            chars={5}
                        />
                    </td>
                </tr>
                <tr>
                    <td>
                        <label class="label-small">Rotate Max</label>
                    </td>
                    <td>
                        <Input
                            number
                            noSendBtn
                            defaultVal={0.6}
                            arrowIncrement={0.01}
                            precision={2}
                            chars={5}
                        />
                    </td>
                </tr>
            </table>
        </div>
    );
};

export default DriverProfiles;
