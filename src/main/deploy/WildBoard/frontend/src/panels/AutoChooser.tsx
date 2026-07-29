import { h } from "preact";
import { useState, useEffect } from "preact/hooks";
import { WsEventBus } from "../ws/WSEventBus";
import FlexRow from "../components/FlexRow";
import Readout from "../components/Readout";
import Dropdown from "../components/Dropdown";

interface Props {
  socket: WsEventBus;
  id: number;

  autos: string[];
}

export default function ({ socket, id, autos }: Props) {
  return (
    <div style="padding: 0.5rem;">
      <FlexRow>
        <label class="label-small" style="margin-right: 0; padding-right: 0;">
          Auto
        </label>
        <Dropdown items={autos} valSetter={(val:string) => socket.send(id, val)} />
      </FlexRow>
    </div>
  );
}
