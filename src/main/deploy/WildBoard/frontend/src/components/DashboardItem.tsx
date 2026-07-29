import { h } from "preact";
import type { ComponentChildren } from "preact";

export default function DashboardItem({ children, noBubble = false }:
    { children:ComponentChildren, noBubble?:boolean }) {

    return (
        <div class={"column-item "+(!noBubble ? " bubble":"")}>
            {children}
        </div>
    )
}