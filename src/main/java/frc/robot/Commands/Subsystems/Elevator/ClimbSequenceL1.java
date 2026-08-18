package frc.robot.Commands.Subsystems.Elevator;

// Import WPILib Libraries
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

// Import Actors, Utils & Constants
import frc.robot.Actors.Subsystems.Elevator.Elevator;

public class ClimbSequenceL1 extends SequentialCommandGroup {

    /**
     * Creates and sets up the ClimbSequenceL1
     * 
     * @param elevatorLift The subsystem to be controlled by the command ({@link ElevatorLift})
     */
    public ClimbSequenceL1(Elevator elevatorLift) {

        addCommands(

            // 1. Full extend
            new ExtendLiftCommand(elevatorLift),

            // 2. Pull down to handoff
            new RetractLiftCommand(elevatorLift, true)
        );
    }
    
}