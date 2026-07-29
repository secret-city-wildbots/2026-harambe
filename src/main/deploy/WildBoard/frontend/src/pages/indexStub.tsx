import { h, Fragment } from "[DEPLOY]/WildBoard/frontend/src/node_modules/preact";
import { useMemo, useState } from "[DEPLOY]/WildBoard/frontend/src/node_modules/preact/hooks";
import { CompModeContext } from "[DEPLOY]/WildBoard/frontend/src/ws/CompModeContext.tsx";
import TabbedContainer from "[DEPLOY]/WildBoard/frontend/src/components/TabbedContainer.tsx";
import Container from "[DEPLOY]/WildBoard/frontend/src/components/Container.tsx";
import { WsEventBus } from "[DEPLOY]/WildBoard/frontend/src/ws/WSEventBus.ts";
[IMPORTS]

export default function () {
    const socket = useMemo(
        () => new WsEventBus(`ws://${window.location.hostname}:5805`),
        []
    );

    const [compMode, setCompMode] = useState(false);

    // All tabs generated at compile time
    const allTabs = useMemo(() => [[TABS]], []);

    // Separate TeleOp tab (preserve state)
    const teleOpTab = useMemo(
        () => allTabs.find(tab => tab?.title === "TeleOp"),
        [allTabs]
    );

    // Other tabs (fully removed in CompMode)
    const otherTabs = useMemo(
        () => allTabs.filter(tab => tab?.title !== "TeleOp"),
        [allTabs]
    );

    const sidepanels = useMemo(() => {
        return (
            <>
                [SIDEPANELS]
            </>
        );
    }, [compMode]);

    // Decide which tabs to render: TeleOp always, others only when compMode is off
    const tabsToRender = useMemo(() => {
        if (!teleOpTab) return otherTabs; // fallback if TeleOp missing
        return compMode ? [teleOpTab] : [teleOpTab, ...otherTabs];
    }, [compMode, teleOpTab, otherTabs]);

    return (
        <CompModeContext.Provider value={{ compMode, setCompMode }}>
            <Container>
                <div class="row" style="padding-left: 0rem;">
                    <div class="col column" style="padding: 0;padding-left: 1rem;">
                        <TabbedContainer tabs={tabsToRender} />
                    </div>

                    {sidepanels && (
                        <div
                            class="col column sidepanel"
                            style="flex: 0 0 12rem;"
                        >
                            {sidepanels}
                        </div>
                    )}
                </div>
            </Container>
        </CompModeContext.Provider>
    );
}