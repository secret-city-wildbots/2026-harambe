package frc.robot.Actors.Subsystems.Intake;

import edu.wpi.first.wpilibj2.command.Subsystem;

public interface Intake extends Subsystem {
    public void startIntaking();
    public void startOuttaking();
    public void stop();
    public void startIntakeVoltage(double voltage);
}
