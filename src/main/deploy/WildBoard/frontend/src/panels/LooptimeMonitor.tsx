import { h } from "preact";
import { WsEventBus } from "../ws/WSEventBus";
import FlexRow from "../components/FlexRow";
import WSReadout from "./WSReadout";

interface Props {
  id: number;
  socket: WsEventBus;
}

export default function ({ id, socket }: Props) {
  return (
    <FlexRow>
      <label class="label-small" style="margin-right: 0; padding-right: 0;">
        Loop (ms): 
      </label>
      <WSReadout socket={socket} id={id} defaultText="20" chart min={0} max={50} />
    </FlexRow>
  );
}
