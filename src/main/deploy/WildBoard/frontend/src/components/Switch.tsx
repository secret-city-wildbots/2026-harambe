import { h } from 'preact';
import { useState, useEffect } from 'preact/hooks';

interface SwitchProps {
  initialState?: boolean;
  onColor?: string;
  offColor?: string;
  knobColor?: string;
  disabled?: boolean;
  vertical?: boolean;
  onToggle?: (state: boolean) => void;
}

const Switch = ({
  initialState = false,
  onColor = 'rgb(116,255,6)',
  offColor = '#ccc',
  disabled = false,
  vertical = false,
  onToggle
}: SwitchProps) => {
  const [isOn, setIsOn] = useState(initialState);

  useEffect(() => {
    setIsOn(initialState);
  }, [initialState]);

  const handleClick = () => {
    if (disabled) return;
    const newState = !isOn;
    setIsOn(newState);
    onToggle?.(newState);
  };

  const switchClass = `switch ${isOn ? 'on' : 'off'} ${vertical ? 'vertical' : ''} ${disabled ? 'disabled' : ''}`;

  return (
    <div
      className={switchClass}
      style={{
        backgroundColor: disabled
          ? '#e0e0e0'
          : isOn
          ? onColor
          : offColor,
      }}
      onClick={handleClick}
    >
      <div
        className="knob"
      />
    </div>
  );
};

export default Switch;
