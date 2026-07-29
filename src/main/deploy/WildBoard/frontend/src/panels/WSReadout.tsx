import { h } from "preact";
import { useState, useEffect } from "preact/hooks";
import { WsEventBus } from "../ws/WSEventBus";
import Readout from "../components/Readout";

interface Props {
  defaultText?: string;
  id: number;
  socket: WsEventBus;
  precision?: number;

  chars?: number;
  color?: string;
  small?: boolean;
  angle?: boolean;
  chart?: boolean;
  temperature?: boolean;
  min?: number;
  max?: number;
  lineColor?: string;
}

export default function ({ id, socket, defaultText = "_", chars, min, max, lineColor, precision, color, small, angle = false, chart = false, temperature = false, }: Props) {
  const [text, setText] = useState(defaultText);

  useEffect(() => {
    const unsubscribe = socket.subscribe(id, (incoming: string) => {
      let data = incoming;
      //do nothing if blank
      if (data === undefined || data === null) return;

      //handle numbers
      let numData: number = Number(data);
      if (!Number.isNaN(Number(data))) {
        if (precision !== undefined) {
          if (precision == 0) {
            data = Math.floor(numData) + "";
          } else {
            data = numData.toFixed(precision);
          }
        }
      }

      //push val
      setText(data + "");
    });

    return () => unsubscribe();
  }, [socket, id, precision]);

  return (
    <Readout text={text} angle={angle} temperature={temperature} small={small} color={color} lineColor={lineColor} chart={chart} min={min} max={max} chars={chars}/>
  );
}