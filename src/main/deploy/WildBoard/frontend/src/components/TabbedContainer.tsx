import { h } from "preact";
import { useState, useEffect } from "preact/hooks";
import type { ComponentChildren } from "preact";

export type Tab = {
  title: string;
  content: ComponentChildren;
};

type TabbedContainerProps = {
  tabs: Tab[];
};

export default function TabbedContainer({ tabs }: TabbedContainerProps) {
  const getInitialIndex = () => {
    const param = new URLSearchParams(window.location.search).get("tab");
    const index = tabs.findIndex(
      (tab) => tab.title.toLowerCase() === param?.toLowerCase()
    );
    return index >= 0 ? index : 0;
  };

  const [activeIndex, setActiveIndex] = useState(getInitialIndex);

  useEffect(() => {
    if (!tabs.length) return;

    const safeIndex = Math.min(activeIndex, tabs.length - 1);

    if (safeIndex !== activeIndex) {
      setActiveIndex(safeIndex);
      return;
    }

    const tab = tabs[safeIndex];
    if (!tab) return;

    const url = new URL(window.location.href);
    url.searchParams.set("tab", tab.title.toLowerCase());
    window.history.replaceState(null, "", url.toString());
  }, [activeIndex, tabs]);

  return (
    <div class="container-fluid">
      <div class="tab-bar">
        {tabs.map((tab, index) => (
          <button
            key={index}
            type="button"
            class={
              "tab-selector " + (activeIndex === index ? "bubble" : "")
            }
            onClick={() => setActiveIndex(index)}
            style={{
              fontWeight: activeIndex === index ? "500" : "400",
            }}
          >
            {tab.title}
          </button>
        ))}
      </div>

      <div style={{ paddingTop: "0rem" }}>
        {tabs.map((tab, index) => (
          <div
            key={index}
            hidden={activeIndex !== index}
            style={{
              contain: activeIndex === index
                ? "layout"
                : "layout style paint",
            }}
          >
            {tab.content}
          </div>
        ))}
      </div>
    </div>
  );
}