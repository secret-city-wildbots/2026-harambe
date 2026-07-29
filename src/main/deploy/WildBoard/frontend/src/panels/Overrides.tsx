import { h } from 'preact';
import { useState } from 'preact/hooks';
import Switch from '../components/Switch';
import { useCompMode } from '../ws/CompModeContext';
import { useLimelightOn } from '../ws/LimelightOnContext';
import { WsEventBus } from '../ws/WSEventBus';

interface SwitchItem {
    label: string;
    initialState?: boolean;
    disabled?: boolean;
}

interface SwitchGridProps {
    socket: WsEventBus;
    id: number;

    switches: SwitchItem[];
    columns?: number;
    className?: string;
}

const SwitchGrid = ({
    switches,
    socket,
    id,
    columns = 3,
    className = ''
}: SwitchGridProps) => {
    const [states, setStates] = useState<boolean[]>(Array(switches.length).fill(false));
    const { setCompMode } = useCompMode();
    const { setLimelightOn } = useLimelightOn();

    const handleToggle = (i: number, state: boolean) => {
        setStates(prev => {
            const newStates = [...prev];  // keep it an array
            newStates[i] = state;

            if (switches[i].label === "CompMode") {
                socket.send(id, "C" + (state ? "1":"0"));
                setCompMode(state);
            }
            if (switches[i].label === "Disable Camera Feeds") {
                setLimelightOn(!state);
            }

            socket.send(id, newStates.map(v => v ? "1" : "0").join());

            return newStates;
        });
    };

    return (
        <div
            className={`switch-grid ${className}`}
            style={{ gridTemplateColumns: `repeat(${columns}, 1fr)` }}
        >
            {switches.map((sw, i) => (
                <div
                    key={i}
                    className="switch-grid-item"
                >
                    <label className="label-small switch-grid-label">
                        {sw.label}
                    </label>

                    <Switch
                        offColor='rgb(116,255,6)'
                        onColor="#ef0001"
                        initialState={states[i]}
                        disabled={sw.disabled}
                        onToggle={(state) => handleToggle(i, state)}
                    />
                </div>
            ))}
        </div>
    );
};

export default SwitchGrid;