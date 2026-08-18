package frc.robot.Commands.Subsystems.Elevator;

// Import WPILib Libraries
import edu.wpi.first.wpilibj2.command.Command;

// Import Actors, Utils & Constants
import frc.robot.Actors.Subsystems.Elevator.Elevator;
import frc.robot.Constants.ElevatorConstants;

public class RetractLiftCommand extends Command {
    // Real Variables
    private final Elevator elevatorLift;
    private final boolean stopAtHandoff;

    /**
     * Creates and sets up the RetractLiftCommand
     * 
     * @param elevatorLift The subsystem to be controlled by the command ({@link ElevatorLift})
     * @param stopAtHandoff Input to control the elevator to stop at handoff or bottom limit switch
     */
    public RetractLiftCommand(Elevator elevatorLift, boolean stopAtHandoff) {
        // Assign the variables and add the subsystem as a requirement to the command
        this.elevatorLift = elevatorLift;
        this.stopAtHandoff = stopAtHandoff;
        addRequirements(this.elevatorLift);
    }

    @Override
    public void initialize() {
        // Call the ElevatorLift subsystem set function
        elevatorLift.setLift(ElevatorConstants.maxSpeedPercentage);
    }

    @Override
    public void execute() {
        // Only use execute if we have dynamically changing speeds. This is called each loop (~20ms).
        // So if we have just a constant speed, use initialize to avoid spamming the canbus network.
    }

    @Override
    public void end(boolean interrupted) {
        // When the command is interrupted or cancelled, we will stop the ElevatorLift subsystem
        elevatorLift.setLift(0.0);
    }

    @Override
    public boolean isFinished() {
        // check to see where to end the command
        if (this.stopAtHandoff) {
            // End the command when we reach the handoff limit
            return elevatorLift.handoffLimitActive();
        } else {
            // End the command when we reach the lower limit
            return elevatorLift.lowerLimitActive();
        }
    }
}