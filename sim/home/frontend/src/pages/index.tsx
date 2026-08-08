import { h, Fragment } from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/node_modules/preact";
import { useMemo, useState } from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/node_modules/preact/hooks";
import { CompModeContext } from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/ws/CompModeContext.tsx";
import TabbedContainer from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/components/TabbedContainer.tsx";
import Container from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/components/Container.tsx";
import { WsEventBus } from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/ws/WSEventBus.ts";
import Checklist from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/Checklist.tsx";

import FieldMap from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/FieldMap.tsx";

import CameraFeed from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/CameraFeed.tsx";
import CameraFeed from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/CameraFeed.tsx";
import DashboardSubRow from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/DashboardSubRow.tsx";
import CameraFeed from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/CameraFeed.tsx";
import CameraFeed from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/CameraFeed.tsx";
import DashboardSubRow from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/DashboardSubRow.tsx";

import AutoChooser from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/AutoChooser.tsx";
import Overrides from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/Overrides.tsx";


import SwerveModules from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/SwerveModules.tsx";
import SystemsCheck from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/SystemsCheck.tsx";

import NumberDisplay from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/NumberDisplay.tsx";

import SimpleSubsystem from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/SimpleSubsystem.tsx";
import SimpleSubsystem from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/SimpleSubsystem.tsx";
import DashboardSubRow from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/DashboardSubRow.tsx";
import SimpleSubsystem from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/SimpleSubsystem.tsx";
import SimpleSubsystem from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/SimpleSubsystem.tsx";
import DashboardSubRow from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/DashboardSubRow.tsx";


import AutoTools from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/AutoTools.tsx";


import LooptimeMonitor from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/LooptimeMonitor.tsx";
import PingMonitor from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/PingMonitor.tsx";
import FPSMonitor from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/FPSMonitor.tsx";
import MasterAlarms from "C:/Users/wildr/Programming/Robotics/FRC-4265/2026-harambe/src/main/deploy/WildBoard/frontend/src/panels/MasterAlarms.tsx";


export default function () {
    const socket = useMemo(
        () => new WsEventBus(`ws://${window.location.hostname}:5805`),
        []
    );

    const [compMode, setCompMode] = useState(false);

    // All tabs generated at compile time
    const allTabs = useMemo(() => [{title: "Checklist",content: (<Container><div class="row"><Checklist ></Checklist></div></Container>)},{title: "TeleOp",content: (<Container><div class="row"><div class="col-2 column"><div class="column-item bubble"><FieldMap socket={socket} id={4} alliance ></FieldMap></div></div><div class="col-6 column"><DashboardSubRow><div class="bubble"><CameraFeed port={11} ></CameraFeed></div><div class="bubble"><CameraFeed port={12} ></CameraFeed></div></DashboardSubRow><DashboardSubRow><div class="bubble"><CameraFeed port={13} ></CameraFeed></div><div class="bubble"><CameraFeed port={14} ></CameraFeed></div></DashboardSubRow></div><div class="col-4 column"><div class="column-item bubble"><AutoChooser socket={socket} id={5} ></AutoChooser></div><div class="column-item bubble"><Overrides socket={socket} columns={2} id={6} switches={[{ label: "Limelight PowerSaver"},{ label: "Disable Camera Feeds"},{ label: "CompMode"},{ label: "Disable Shot Smoothing"},{ label: "Always Aim at Hub"},{ label: "Disable Shoot Safeties"}]} ></Overrides></div></div></div></Container>)},{title: "Subsystems",content: (<Container><div class="row"><div class="col-4 column"><div class="column-item bubble"><SwerveModules socket={socket} id={7} ></SwerveModules></div><div class="column-item bubble"><SystemsCheck socket={socket} id={8} ></SystemsCheck></div></div><div class="col-3 column"><div class="column-item bubble"><NumberDisplay socket={socket} label={"BPS: "} id={9} ></NumberDisplay></div></div><div class="col-5 column"><DashboardSubRow><div class="bubble"><SimpleSubsystem socket={socket} name={"Shooter"} id={10} velocity ></SimpleSubsystem></div><div class="bubble"><SimpleSubsystem socket={socket} name={"Intake"} unit={"rps"} id={11} absolute ></SimpleSubsystem></div></DashboardSubRow><DashboardSubRow><div class="bubble"><SimpleSubsystem socket={socket} name={"Transfer"} id={12} velocity ></SimpleSubsystem></div><div class="bubble"><SimpleSubsystem socket={socket} name={"Indexer"} id={13} velocity ></SimpleSubsystem></div></DashboardSubRow></div></div></Container>)},{title: "Autos",content: (<Container><div class="row"><div class="col-12 column"><div class="column-item bubble"><AutoTools socket={socket} id={14} ></AutoTools></div></div></div></Container>)},], []);

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
                <div class="column-item" style="padding-bottom: 0;">
<LooptimeMonitor socket={socket} id={0} ></LooptimeMonitor>
</div><div class="column-item" style="padding-bottom: 0;">
<PingMonitor socket={socket} id={1} ></PingMonitor>
</div><div class="column-item" style="padding-bottom: 0;">
<FPSMonitor socket={socket} id={2} ></FPSMonitor>
</div><div class="column-item" style="padding-bottom: 0;">
<MasterAlarms socket={socket} cols={2} id={3} descriptions={["Swerve Overheat","Subsystem Overheat","Canbus Error","Current High","Joystick Disconnect","Ping High/Failed","Loop Time too High","Battery Voltage Low","Placeholder for future issues","Placeholder for future issues"]} texts={["SWOH","SSOH","CNBS","CURR","JOYS","PING","LOOP","BATT","blank","blank"]} ></MasterAlarms>
</div>
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