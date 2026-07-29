import { h } from "preact";
import { useState, useEffect } from "preact/hooks";
import { WsEventBus } from "../ws/WSEventBus";
import FlexRow from "../components/FlexRow";
import Readout from "../components/Readout";

interface Props {
  socket: WsEventBus;
  id: number;
}

export default function ({ socket, id }: Props) {
  const [text, setText] = useState(socket.latency+"");

  useEffect(() => {
    const unsubscribe = socket.subscribeFPS((fps: number) => {
      setText((Math.round(fps)+""));
    });

    return () => unsubscribe();
  }, [socket]);

  return (
    <FlexRow>
      <label class="label-small" style="margin-right: 0; padding-right: 0;">
        FPS (f/s):
      </label>
      <Readout text={text} chart min={0} max={500} chars={3} />
    </FlexRow>
  );
}
