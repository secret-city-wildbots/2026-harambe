package frc.robot.Actors.Subsystems.Elevator;

import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

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

    /**
     * The requirement token for the lift motor. Lift commands add this instead of the
     * whole Elevator, so a lift command and a hook command can share a parallel group.
     */
    public Subsystem liftRequirement();

    /**
     * The requirement token for the hook motor. See {@link #liftRequirement()}.
     */
    public Subsystem hookRequirement();

    /**
     * A do-nothing subsystem used only as a requirement token. It owns no hardware and
     * has no periodic work: the Elevator implementation still does all of the driving.
     * Its only job is to let the scheduler tell "the lift" and "the hooks" apart.
     */
    public class Requirement extends SubsystemBase {
        public Requirement(String name) {
            setName(name);
        }
    }
}
