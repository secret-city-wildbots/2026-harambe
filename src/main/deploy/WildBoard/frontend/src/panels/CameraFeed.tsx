import { h } from "preact";
import { useState } from "preact/hooks";
import { useLimelightOn } from "../ws/LimelightOnContext";

interface LimelightProps {
  port: number;
  aspectRatio?: number; // width / height, default 1.6
}

export default function Limelight({ port, aspectRatio = 1.6 }: LimelightProps) {
  const [error, setError] = useState(false);
  const { limelightOn } = useLimelightOn();

  return (
    <img
      src={`http://10.42.65.${(limelightOn) ? port:100}:5800`}
      alt={`Limelight Disconnected src: 10.42.65.${port}:5801`}
      onError={() => setError(true)}
      onLoad={() => setError(false)}
      style={{
        width: "100%",
        height: error ? `calc(100% / ${aspectRatio})` : "auto",
        aspectRatio: error ? `${aspectRatio}` : undefined,
        objectFit: "contain",
        display: "block",
      }}
    />
  );
}
