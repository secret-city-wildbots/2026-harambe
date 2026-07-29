import { h } from "preact";
import { useState, useEffect } from "preact/hooks";
import { WsEventBus } from "../ws/WSEventBus";
import Readout from "../components/Readout";
import ArmableButton from "../components/ArmableButton";

interface Props {
  id: number;
  socket: WsEventBus;
}

export default function ({ id, socket }: Props) {
  return (
    <div
      style={{
        //vertically align in column
        display: "flex",
        justifyContent: "center",
        flexDirection: "column",
        padding: "1rem",
        paddingTop: "0.25rem",
        paddingBottom: "0.75rem",
      }}
    >
      <label style="padding-bottom: 0.25rem;">Systems Check</label>
      <ArmableButton
        text="TEST"
        onClick={() => socket.send(id, "C1")}
        onRelease={() => socket.send(id, "C0")}
      />
    </div>
  );
}
