import { h } from "preact";
import type { ComponentChildren } from "preact";

export default function Container({
    children,
}: {
    children: ComponentChildren;
}) {
    return <div class="container-fluid row">{children}</div>;
}
