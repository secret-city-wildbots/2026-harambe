package frc.robot.Commands.Subsystems.Drivetrain;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathConstraints;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class AutoAlignToClimb extends SequentialCommandGroup {

    public AutoAlignToClimb() {
        // Assign the variables and add the subsystem as a requirement to the command

        addCommands(

            new SequentialCommandGroup(

                AutoBuilder.pathfindToPose(
                    new Pose2d(2.5, 3.317, Rotation2d.fromDegrees(0)),
                    new PathConstraints(
                        2.0, 4.0,
                        Units.degreesToRadians(360), Units.degreesToRadians(540)),
                    0),

                AutoBuilder.pathfindToPose(
                    new Pose2d(1.45, 3.317, Rotation2d.fromDegrees(0)),
                    new PathConstraints(
                        2.0, 1.0,
                        Units.degreesToRadians(360), Units.degreesToRadians(540)),
                    0)));
    }
}
