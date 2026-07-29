import { h, Fragment } from 'preact';
import { useRef, useEffect } from 'preact/hooks';

interface AngleDisplayProps {
  angle: number; //in degrees
  zero?: number;
  radius?: number; //in pixels
  counterClockwise?: boolean;
}

const AngleDisplay = ({
  angle,
  counterClockwise = false,
  zero = -90,
  radius = 30,
}: AngleDisplayProps) => {
  const canvasRef = useRef(null);

   useEffect(() => {

        angle = ((angle+zero)/180.0*Math.PI*(counterClockwise?-1:1));

        const canvas = canvasRef.current;
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        if (ctx) {

            ctx.clearRect(-100,-100,10000,10000);

            ctx.beginPath();
            ctx.arc(radius/2, radius/2, radius/2, 0, Math.PI * 2);
            ctx.fillStyle = "rgb(255, 1, 152)";
            ctx.fill();

            ctx.beginPath();
            ctx.arc(radius/2, radius/2, radius/2-Math.ceil(radius/10), 0, Math.PI * 2);
            ctx.fillStyle = "rgb(51, 51, 94)";
            ctx.fill();

            ctx.beginPath();
            ctx.moveTo(radius/2, radius/2);  // Start point
            ctx.lineTo((Math.cos(angle)*radius*0.5)+(radius/2), (Math.sin(angle)*radius*0.5)+(radius/2));  // End point
            ctx.strokeStyle = 'rgb(68, 142, 205)';
            ctx.lineWidth = Math.ceil(radius/10);
            ctx.stroke();

        }
    }, [angle, radius]);

  return (
    <>
        <canvas ref={canvasRef} width={radius} height={radius}></canvas>
    </>
  );
};

export default AngleDisplay;