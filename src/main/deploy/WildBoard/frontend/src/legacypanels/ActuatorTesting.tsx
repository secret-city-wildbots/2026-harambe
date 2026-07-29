import { h } from "preact";
import { useState } from "preact/hooks";

import { Socket } from "socket.io-client";
import Dropdown from "../components/Dropdown.tsx";
import FlexRow from "../components/FlexRow.tsx";
import NTInput from "../components/NTInput.tsx";

interface ActuatorTestingProps {
    socket: Socket;
    dashboardItem?: boolean;
}

const ActuatorTesting = ({
    socket,
    dashboardItem = false,
}: ActuatorTestingProps) => {
    const [actuator, setActuator] = useState("No_Test");
    const [actuatorNames, setActuatorNames] = useState(["No_Test"]);

    socket.on("Legal_Actuator_Names", (val) => {
        setActuatorNames(val);
    });

    const setActuatorVal = (newVal: string) => {
        setActuator(newVal);
        socket.emit("Test_Actuator_Name", actuator);
    };

    return (
        <div
            class={"actuator-testing" + (dashboardItem ? " column-item" : "")}
            style={{
                display: "flex",
                justifyContent: "center",
                flexDirection: "column",
                wrap: "no-wrap",
            }}
        >
            <table>
                <tr>
                    <th><label class="label-small">Name:</label></th>
                    <th><label class="label-small">Value (dc):</label></th>
                    <th><label class="label-small">Period (s):</label></th>
                </tr>
                <tr>
                    <td>
                        <Dropdown items={actuatorNames} valSetter={setActuatorVal} />
                    </td>
                    <td>
                        <NTInput socket={socket} nt="Test_Actuator_Value" defaultVal={0.0} number precision={2} />
                    </td>
                    <td>
                        <NTInput socket={socket} nt="Test_Actuator_Period" defaultVal={0.0} number precision={1} />
                    </td>
                </tr>
            </table>
        </div>
    );
};

export default ActuatorTesting;
