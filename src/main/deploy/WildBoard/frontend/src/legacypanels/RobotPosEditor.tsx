import { h } from "preact";
import { Socket } from "socket.io-client";
import Input from "../components/Input.tsx";
import Button from "../components/Button.tsx";
import FlexCol from "../components/FlexCol.tsx";

interface RobotPosEditorProps {
  socket: Socket;
}

const RobotPosEditor = ({ socket }: RobotPosEditorProps) => {
  return (
      <FlexCol>
        <div style="padding-bottom: 1rem">
          <Button text="PUSH ROBOT POS" />
        </div>

        <table>
          <tr>
            <td>
              <label>X (in)</label>
            </td>
            <td>
              <Input defaultVal={0.0} number precision={1} />
            </td>
          </tr>
          <tr>
            <td>
              <label>Y (in)</label>
            </td>
            <td>
              <Input defaultVal={0.0} number precision={1} />
            </td>
          </tr>
          <tr>
            <td>
              <label>H (deg)</label>
            </td>
            <td>
              <Input defaultVal={0.0} number precision={1} noSendBtn />
            </td>
          </tr>
        </table>
      </FlexCol>
  );
};

export default RobotPosEditor;
