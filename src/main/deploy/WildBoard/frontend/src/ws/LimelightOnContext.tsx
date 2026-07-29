import { createContext } from 'preact';
import { useContext } from 'preact/hooks';

interface LimelightOnContextType {
    limelightOn: boolean;
    setLimelightOn: (value: boolean) => void;
}

export const LimelightOnContext = createContext<LimelightOnContextType>({
    limelightOn: true,
    setLimelightOn: () => {}
});

export const useLimelightOn = () => useContext(LimelightOnContext);