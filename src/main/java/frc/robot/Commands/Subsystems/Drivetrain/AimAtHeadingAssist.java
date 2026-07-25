package frc.robot.Commands.Subsystems.Drivetrain;

import java.util.function.Supplier;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.Actors.Subsystems.CommandSwerveDrivetrain;

public class AimAtHeadingAssist extends SequentialCommandGroup {
    private final CommandSwerveDrivetrain drivetrain;
    private final Supplier<Rotation2d> target;
    private PIDController pid;
    private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
            .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive motors

    public AimAtHeadingAssist(CommandSwerveDrivetrain drivetrain, Supplier<Rotation2d> target, Supplier<Double> velx, Supplier<Double> vely) {
        // Assign the variables and add the subsystem as a requirement to the command
        this.drivetrain = drivetrain;
        this.target = target;
        addRequirements(drivetrain);

        this.pid = new PIDController(0.1, 0, 0);
        this.pid.enableContinuousInput(0, 360);

        addCommands(this.drivetrain.applyRequest(
                () -> drive.withRotationalRate(this.pid.calculate(drivetrain.getPigeon2().getYaw().getValueAsDouble(), this.target.get().getDegrees())).withVelocityX(velx.get()).withVelocityY(vely.get())
            ));
    }
}
