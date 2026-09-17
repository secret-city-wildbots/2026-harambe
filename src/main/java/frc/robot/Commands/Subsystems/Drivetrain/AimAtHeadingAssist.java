package frc.robot.Commands.Subsystems.Drivetrain;

import java.util.function.Supplier;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;

import dev.doglog.DogLog;

import com.ctre.phoenix6.swerve.SwerveRequest;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.Actors.Subsystems.CommandSwerveDrivetrain;

public class AimAtHeadingAssist extends SequentialCommandGroup {
    private final CommandSwerveDrivetrain drivetrain;
    private final Supplier<Rotation2d> target;
    private ProfiledPIDController pid;
    private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
            .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive motors

    public AimAtHeadingAssist(CommandSwerveDrivetrain drivetrain, Supplier<Rotation2d> target, Supplier<Double> velx,
            Supplier<Double> vely) {
        // Assign the variables and add the subsystem as a requirement to the command
        this.drivetrain = drivetrain;
        this.target = target;
        addRequirements(drivetrain);

        this.pid = new ProfiledPIDController(
                0.4, 0.0, 0.02, new TrapezoidProfile.Constraints(360, 720));
        this.pid.enableContinuousInput(0, 360);

        addCommands(this.drivetrain.applyRequest(
                () -> {
                    DogLog.forceNt.log("targetRot", this.target.get().getDegrees());
                    DogLog.forceNt.log("currentRot", drivetrain.getPigeon2().getYaw().getValueAsDouble()%360);
                    return drive
                            .withRotationalRate(this.pid.calculate(drivetrain.getPigeon2().getYaw().getValueAsDouble(),
                                    this.target.get().getDegrees()))
                            .withVelocityX(velx.get()).withVelocityY(vely.get());
                }));
    }
}
