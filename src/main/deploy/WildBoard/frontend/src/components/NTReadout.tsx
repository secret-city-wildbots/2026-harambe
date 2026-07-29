import { h } from "preact";
import { useState, useEffect } from "preact/hooks";
import Readout from "./Readout.tsx";
import { useSocketStore } from "../socket/SocketProvider.tsx";

interface NTReadoutProps {
  nt: string;
  defaultVal?: any;
  chars?: number;
  precision?: number;
  color?: string;
  small?: boolean;
  index?: number;
  angle?: boolean;
  temperature?: boolean;
}

const NTReadout = ({
  nt,
  defaultVal = "null",
  chars,
  precision,
  color = '#eed',
  small,
  index,
  angle = false,
  temperature = false,
}: NTReadoutProps) => {
  const socket = useSocketStore();
  const [value, setValue] = useState(socket?.pullVal(nt));

  useEffect(() => {
    const handler = (newVal: any) => {
      if (newVal !== undefined && newVal !== null) {
        if (Array.isArray(newVal) && typeof index === "number") {
          newVal = newVal[index];
        }
        if (precision !== undefined) {
          if (precision == 0) {
            newVal = Math.floor(newVal);
          } else {
            newVal = newVal.toFixed(precision);
          }
        }
        setValue(newVal);
      }
    };

    // initialize with initial value
    handler(value);

    if (socket) socket.on(nt, handler);

    // Cleanup listener on unmount or if nt/socket changes
    return () => {
      if (socket) socket.off(nt, handler);
    };
  }, [nt, index, precision]); // re-register only if these change

  return (
    <Readout text={value !== undefined && value !== null ? value:null} angle={angle} temperature={temperature} small={small} color={color} chars={chars} />
  );
};

export default NTReadout;
