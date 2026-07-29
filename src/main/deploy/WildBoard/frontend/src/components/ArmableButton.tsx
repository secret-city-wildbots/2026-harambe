import { h } from 'preact';
import { useState } from "preact/hooks";

import Button from "./Button.tsx";
import Switch from "./Switch.tsx";
import FlexRow from "./FlexRow.tsx";

interface ArmableButtonProps {
  text: string;
  onClick?: () => void;
  onRelease?: () => void;
  onToggle?: (on:boolean) => void;
  disabled?: boolean;
  btnColor?: string;
}

const ArmableButton = ({
  text,
  onClick,
  onRelease,
  onToggle,
  disabled = true,
  btnColor = 'rgb(68,142,205)',
}: ArmableButtonProps) => {
  const [isDisabled, setIsDisabled] = useState(disabled);

  return (
    <FlexRow noPadding>
        <div style="margin-right: 10px; height: 100%;">
            <Button text={text}
                color={btnColor}
                disabled={isDisabled}
                onClick={onClick}
                onRelease={onRelease}
            />
        </div>
        <Switch onColor="#ef0001" offColor="rgba(116, 255, 6, 1)" onToggle={(on:boolean) => { setIsDisabled(!on); onToggle?.(on) }} />
    </FlexRow>
  );
};

export default ArmableButton;