import { h } from "preact";
import { useState, useEffect } from "preact/hooks";
import { WsEventBus } from "../ws/WSEventBus";
import FlexRow from "../components/FlexRow";
import Readout from "../components/Readout";

interface Props {
  socket: WsEventBus;
  id: number;
  label: string;
  chars?: number;
}

export default function ({ socket, id, label, chars=3 }: Props) {
  const [text, setText] = useState("_");

  useEffect(() => {
    const unsubscribe = socket.subscribe(id, (msg: string) => {
      setText(msg);
    });

    return () => unsubscribe();
  }, [socket]);

  return (
    <FlexRow>
      <label class="label-small" style="margin-right: 0; padding-right: 0;">
        {label}
      </label>
      <Readout text={text} chars={chars} />
    </FlexRow>
  );
}