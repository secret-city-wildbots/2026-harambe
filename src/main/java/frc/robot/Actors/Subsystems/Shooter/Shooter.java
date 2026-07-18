package frc.robot.Actors.Subsystems.Shooter;

import edu.wpi.first.wpilibj2.command.Subsystem;

public interface Shooter extends Subsystem {
    public void startShooting();
    public void stop();
    public void periodic();
}