package frc.robot.Commands.Subsystems.Intake;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Actors.Subsystems.Intake.Intake;

public class RetractIntake extends Command {
    private final Intake intake;

    /**
     * Creates and sets up the ExtensionCommand
     * 
     * @param intakeExtension The subsystem to be controlled by the command ({@link IntakeExtension})
     * @param intakeExtensionPos The pos for the motor
     */
    public RetractIntake(Intake intake) {
        // Assign the variables and add the subsystem as a requirement to the command
        this.intake = intake;
        addRequirements(intake);
    }

    @Override
    public void initialize() {
        // Call the intakeExtension subsystem setIntakePos function
        intake.stop();
    }

    @Override
    public void execute() {
        // Only use execute if we have dynamically changing speeds. This is called each loop (~20ms).
        // So if we have just a constant speed, use initialize to avoid spamming the canbus network.
    }

    @Override
    public void end(boolean interrupted) {
    }

    @Override
    public boolean isFinished() {
        // Do not end the command
        return true;
    }
}
