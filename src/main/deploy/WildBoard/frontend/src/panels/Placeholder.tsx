import { h } from "preact";

interface Props {
  text: String;
  height?: number;
}

export default function({text, height = 5}: Props) {

    return (
        <div style={`width: 100%; text-align: center; height:${height}rem; font-size: 2rem;`}>
            {text}
        </div>
    );
};