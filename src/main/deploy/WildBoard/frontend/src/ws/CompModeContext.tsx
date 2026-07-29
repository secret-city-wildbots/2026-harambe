import { createContext } from 'preact';
import { useContext } from 'preact/hooks';

interface CompModeContextType {
    compMode: boolean;
    setCompMode: (value: boolean) => void;
}

export const CompModeContext = createContext<CompModeContextType>({
    compMode: false,
    setCompMode: () => {}
});

export const useCompMode = () => useContext(CompModeContext);