package frc.robot.Actors.Subsystems.Elevator;

import edu.wpi.first.wpilibj2.command.Subsystem;

public class ElevatorSim implements Elevator {

    // Requirement tokens so lift commands and hook commands can run at the same time
    private final Requirement liftReq = new Requirement("ElevatorLift");
    private final Requirement hookReq = new Requirement("ElevatorHooks");

    public Subsystem liftRequirement() {
        return this.liftReq;
    }

    public Subsystem hookRequirement() {
        return this.hookReq;
    }

    public ElevatorSim() {

    }
    public double getTemp() {
        return 0.0;
    }
    public void setHooks(double percent) {

    }
    public void setLift(double percent) {

    }
    public boolean lowerLimitActive() {
        return false;
    }
    public boolean handoffLimitActive() {
        return false;
    }
    public boolean topLimitActive() {
        return false;
    }
    public void setTargetAngle(double angle) {

    }
    public boolean climbAfterTopLimitSwitch() {
        return false;
    }
    public double getCurrentAngle() {
        return 0.0;
    }
    public double getTargetAngle() {
        return 0.0;
    }
    public void periodic() {
        
    }
}