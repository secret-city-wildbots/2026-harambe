import { ComponentChildren, h } from 'preact';

interface ButtonProps {
  text: string|ComponentChildren;
  onClick?: () => void;
  onRelease?: () => void;
  disabled?: boolean;
  color?: string;
  style?: {};
}

const Button = ({
  text,
  onClick,
  onRelease,
  disabled = false,
  color = 'rgb(68,142,205)',
  style,
}: ButtonProps) => {
  const buttonClass = `button${disabled ? ' disabled' : ''}`;

  return (
    <button
      className={buttonClass}
      type="button"
      onMouseDown={() => !disabled && onClick?.()}
      onMouseUp={() => !disabled && onRelease?.()}
      style={{
        backgroundColor: color,
        ...(style ? style:{})
      }}
      disabled={disabled}
    >
      {text}
    </button>
  );
};

export default Button;