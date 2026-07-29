import { h } from "preact";
import type { ComponentChildren } from "preact";

export default function FlexCol({
    children,
    noPadding = false,
}: {
    children: ComponentChildren[];
    noPadding?: boolean;
}) {
    return (
        <div style="display: flex; justify-center: center; align-items: center; flex-direction:column;">
            {noPadding
                ? children
                : children.map((item, index) => {
                      return (
                          // apply styles padding only when the item isn't the last one
                          <div
                              style={
                                  index < children.length
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
