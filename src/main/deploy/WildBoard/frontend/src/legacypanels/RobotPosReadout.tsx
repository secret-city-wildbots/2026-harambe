import { h } from "preact";
import { Socket } from "socket.io-client";
import Input from "../components/Input.tsx";
import Button from "../components/Button.tsx";
import FlexCol from "../components/FlexCol.tsx";
import Readout from "../components/Readout.tsx";
import NTReadout from "../components/NTReadout.tsx";

interface RobotPosReadoutProps {
  socket: Socket;
}

const RobotPosReadout = ({ socket }: RobotPosReadoutProps) => {
  return (
        <table>
          <tr>
            <td>
              <label class="label-small">Current X (in)</label>
            </td>
            <td>
              <NTReadout nt="Robot_X" socket={socket} precision={1} chars={4} />
            </td>
          </tr>
          <tr>
            <td>
              <label class="label-small">Current Y (in)</label>
            </td>
            <td>
              <NTReadout nt="Robot_Y" socket={socket} precision={1} chars={4} />
            </td>
          </tr>
          <tr>
            <td>
              <label class="label-small">Current H (deg)</label>
            </td>
            <td>
              <NTReadout nt="Robot_H" socket={socket} precision={1} chars={4} />
            </td>
          </tr>
        </table>
  );
};

export default RobotPosReadout;
