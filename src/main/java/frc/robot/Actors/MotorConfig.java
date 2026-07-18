package frc.robot.Actors;

// Import Actors, Utils & Constants
import frc.robot.Utils.RotationDir;

public class MotorConfig {
    public double peakForwardDC = 1.0;
    public double peakReverseDC = -1.0;
    public boolean brake = true;
    public RotationDir direction = RotationDir.Clockwise;
    public boolean forwardLimitSwitchEnabled = false;
    public boolean reverseLimitSwitchEnabled = false;
    public double dutyCycleOpenLoopRampPeriod = 0.0;
    public double dutyCycleClosedLoopRampPeriod = 0.0;
}