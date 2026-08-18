package frc.robot.Actors.Subsystems.Elevator;

import edu.wpi.first.wpilibj2.command.Subsystem;

public interface Elevator extends Subsystem {
    public double getTemp();
    public void setHooks(double percent);
    public void setLift(double percent);
    public boolean lowerLimitActive();
    public boolean handoffLimitActive();
    public boolean topLimitActive();
    public void setTargetAngle(double angle);
    public boolean climbAfterTopLimitSwitch();
    public double getCurrentAngle();
    public double getTargetAngle();
    public void periodic();
}