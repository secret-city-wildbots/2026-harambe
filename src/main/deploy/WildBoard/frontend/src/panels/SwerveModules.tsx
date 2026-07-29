import { h, Fragment } from 'preact';
import ArmableButton from "../components/ArmableButton.tsx";
import Button from "../components/Button.tsx";
import { WsEventBus } from '../ws/WSEventBus.ts';
import { useState, useEffect, useRef } from 'preact/hooks';
import Readout from '../components/Readout.tsx';

interface Props {
  socket: WsEventBus;
  id: number;
  dashboardItem?: boolean;
  shifting?: boolean;
}

export default function ({
    socket,
    id,
    dashboardItem = false,
    shifting = false,
}: Props) {

    const [state, setState] = useState(new Array(12).fill(0));
    // keep last-received payload to avoid state updates when nothing changed
    const lastPayloadRef = useRef<string | null>(null);

    // helpful mount/unmount logs to verify real remounts (runs only on mount)
    useEffect(() => {
        console.log("Mounted SwerveModules id=", id);
        return () => {
            console.log("Unmounted SwerveModules id=", id);
        };
    }, []);

    // Subscribe once and clean up on unmount. WsEventBus.subscribe returns an unsubscribe function.
    useEffect(() => {
        const unsubscribe = socket.subscribe(id, (data: string) => {
            try {
                const newData = data.split(",").map((str, i) => Number(str).toFixed((i < 4) ? 0 : 1));

                if (newData.length != 12) throw new Error();

                const serialized = newData.join(",");
                // skip update if identical to last payload
                if (serialized === lastPayloadRef.current) return;

                lastPayloadRef.current = serialized;
                setState(newData);
            } catch (err: any) {
                console.error("[Swerve Modules] Bad packet recieved");
            }
        });

        return () => {
            unsubscribe();
        };
    }, [socket, id]);

    const onToggleUnlock = (on: boolean) => {
        socket.send(id, on ? "ul_a":"l_a");
    };

    const onClickCalib = () => {
        socket.send(id, "c1");
    };

    const onReleaseCalib = () => {
        socket.send(id, "c0");
    };

    const onClickHome = () => {
        socket.send(id, "h1");
    };

    const onReleaseHome = () => {
        socket.send(id, "h0");
    };

  return (
    <div class={"swerve-modules " + (dashboardItem ? "column-item":"")} style={{
        display: "flex",
        justifyContent: "center",
        flexDirection: "column",
        wrap: "no-wrap",
    }}>
        <table>
            <tr>
                <th>

                </th>
                <th>
                    Module 1
                </th>
                <th>
                    Module 0
                </th>
            </tr>
            <tr>
                <td>
                    <label class="label-small">
                        Angle (deg)
                    </label>
                </td>
                <td>
                    <Readout text={state[0]} chars={4} angle />
                </td>
                <td>
                    <Readout text={state[1]} chars={4} angle />
                </td>
            </tr>
            <tr>
                <td>
                    <label class="label-small">
                        Temp (C)
                    </label>
                </td>
                <td>
                    <Readout text={state[4]} chars={4} temperature chart />
                </td>
                <td>
                    <Readout text={state[5]} chars={4} temperature chart />
                </td>
            </tr>
            <tr>
                <td>
                    <label class="label-small">
                        Vel (ft/s)
                    </label>
                </td>
                <td>
                    <Readout text={state[8]} chars={4} chart min={0} max={8} />
                </td>
                <td>
                    <Readout text={state[9]} chars={4} chart min={0} max={8} />
                </td>
            </tr>
        </table>
        <table class="mt-3">
            <tr>
                <th>

                </th>
                <th>
                    Module 2
                </th>
                <th>
                    Module 3
                </th>
            </tr>
            <tr>
                <td>
                    <label class="label-small">
                        Angle (deg)
                    </label>
                </td>
                <td>
                    <Readout text={state[2]} chars={4} angle />
                </td>
                <td>
                    <Readout text={state[3]} chars={4} angle />
                </td>
            </tr>
            <tr>
                <td>
                    <label class="label-small">
                        Temp (C)
                    </label>
                </td>
                <td>
                    <Readout text={state[6]} chars={4} temperature chart />
                </td>
                <td>
                    <Readout text={state[7]} chars={4} temperature chart />
                </td>
            </tr>
            <tr>
                <td>
                    <label class="label-small">
                        Vel (ft/s)
                    </label>
                </td>
                <td>
                    <Readout text={state[10]} chars={4} chart min={0} max={8} />
                </td>
                <td>
                    <Readout text={state[11]} chars={4} chart min={0} max={8} />
                </td>
            </tr>
        </table>
        {/*<div style="display: flex; justify-center: center; align-items: center; padding-top: 1rem;">
            <div style="padding-right: 0.4rem;">
                <Button text="HOME" onClick={onClickHome} onRelease={onReleaseHome}/>
            </div>
            <ArmableButton text="CALIB" onClick={onClickCalib} onRelease={onReleaseCalib} />
        </div>*/}
    </div>
  );
};