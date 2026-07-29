import { h } from "preact";
import type { ComponentChildren } from "preact";

export default function DashboardSubRow({
    children,
}: {
    children: ComponentChildren[];
}) {
    return (
        <div class="row d-flex align-items-stretch column-item p-0">
            {children.map((item, index) => {
                return (
                    // apply styles padding only when the item isn't the last one
                    <div
                        class="col"
                        style={
                            index < children.length-1
                                ? { paddingRight: "0.5rem" }
                                : {}
                        }
                    >
                        {item}
                    </div>
                );
            })}
        </div>
    );
}
