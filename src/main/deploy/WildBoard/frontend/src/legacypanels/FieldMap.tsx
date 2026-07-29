import { h, Fragment } from 'preact';
import { Socket } from "socket.io-client";
import { MutableRef, useEffect, useRef } from "preact/hooks";

interface FieldMapProps {
  socket: Socket,
}

const FieldMap = ({
    socket,
}: FieldMapProps) => {
    const canvasRef = useRef(null);
    const imageRef = useRef(null);

    function render(canvasRef:MutableRef<null>, imageRef:MutableRef<null>) {
        const canvas = canvasRef.current;
        if (!canvas) return;
        if (!imageRef.current) return;

        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        const robotPos = [24, 24];
        const robotSize = [21.75, 23.75];

        const bottomLeft = [289, 3465];

        const fieldWidth = 317;
        const fieldWidthpx = 1634-289;

        const fieldScale = fieldWidthpx/fieldWidth;
        
        const robotPospx = [robotPos[0] * fieldScale + bottomLeft[0], robotPos[1] * fieldScale + bottomLeft[1]];
        const robotSizepx = [robotSize[0] * fieldScale, robotSize[1] * fieldScale];

        ctx.drawImage(imageRef.current, 0, 0);
        
        ctx.lineWidth = 20;
        ctx.strokeStyle = "red";

        ctx.strokeRect(robotPospx[0],canvas.height-robotPospx[1],robotSizepx[0],robotSizepx[1]);
    }

    useEffect(() => {
        render(canvasRef, imageRef);
    }, [canvasRef, imageRef]);

    setTimeout(() => {
        render(canvasRef, imageRef);
    }, 200);

    return (
        <>
            <canvas ref={canvasRef} width={1926} height={3999} style="width: 193px;"></canvas>
            <img ref={imageRef} style="display:none" src={`/assets/fieldmap${"red"}.png`} />
        </>
    );
};

export default FieldMap;