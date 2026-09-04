package frc.robot.Commands.Subsystems.Elevator;

// Import WPILib Libraries
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

// Import Actors, Utils & Constants
import frc.robot.Constants.ElevatorConstants;
import frc.robot.Actors.Subsystems.Elevator.Elevator;

public class ClimbSequenceL3 extends SequentialCommandGroup {

    /**
     * Creates and sets up the ClimbSequenceL3
     * 
     * @param elevatorLift The subsystem to be controlled by the command ({@link ElevatorLift})
     * @param hook The subsystem to be controlled by the command ({@link ElevatorHook})
     */
    public ClimbSequenceL3(Elevator elevator) {

        addCommands(

            // 1. Full extend & drop guide
            new ParallelCommandGroup(
                new ExtendLiftCommand(elevator),
                new RotateHookToPositionCommand(elevator, ElevatorConstants.hookGuideDeployedPosition)
            ),

            // // 2. Allow hooks to extend out fully
            // new ClimbAfterTopLimitSwitch(lift),

            // 3. Pull down to handoff
            new RetractLiftCommand(elevator, true),

            // 4. Rotate hooks out
            new RotateHookToPositionCommand(elevator, ElevatorConstants.hookDeployedPosition),

            // 5. Full extend
            new ExtendLiftCommand(elevator),

            // // 6. Allow hooks to extend out fully
            // new ClimbAfterTopLimitSwitch(lift),

            // 7. Allow hooks to extend out fully
            new RotateHookToPositionCommand(elevator, ElevatorConstants.hookPosForTopRungClearance),

            // 8. Pull down AND rotate hooks safe (parallel)
            new ParallelCommandGroup(
                new RetractLiftCommand(elevator, true),
                new RotateHookToPositionCommand(elevator, ElevatorConstants.hookSafePosition)
            ),

            // 9. Rotate hooks out
            new RotateHookToPositionCommand(elevator, ElevatorConstants.hookDeployedPosition),

            // 10. Full extend
            new ExtendLiftCommand(elevator),

            // // 11. Allow hooks to extend out fully
            // new ClimbAfterTopLimitSwitch(lift),

            // 12. Allow hooks to extend out fully
            new RotateHookToPositionCommand(elevator, ElevatorConstants.hookPosForTopRungClearance),

            // 13. Pull down to handoff
            new ParallelCommandGroup(
                new RetractLiftCommand(elevator, true),
                new RotateHookToPositionCommand(elevator, ElevatorConstants.hookSafePosition))
        );
    }
    
}
