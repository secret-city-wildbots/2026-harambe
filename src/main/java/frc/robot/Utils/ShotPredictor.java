package frc.robot.Utils;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.robot.Actors.Subsystems.CommandSwerveDrivetrain;

public class ShotPredictor {
    public static double hubX = (DriverStation.getAlliance().get() == Alliance.Blue) ? (4.63) : (11.9);
    public static Translation2d hubPosition = new Translation2d(hubX, 4.035);

    public static double getShotVel(Translation2d pose, ChassisSpeeds robotVel) {
        double dist = pose.getDistance(ShotPredictor.getAdjustedHub(robotVel, pose.getDistance(hubPosition)));
        System.out.println(dist);

        // return SmartDashboard.getNumber("pow", 0.0);
        return 6.6 + (dist - 1.68) * (0.392) + ((dist > 2.4) ? (dist - 2.4) * 0.35 : 0);
    }

    public static double getShotRPS(Translation2d pose, ChassisSpeeds robotVel) {
        double dist = pose.getDistance(ShotPredictor.getAdjustedHub(robotVel, pose.getDistance(hubPosition)));
        return 7.70997 * (dist - 2) + 45.16995;
    }

    public static Translation2d getAdjustedHub(ChassisSpeeds robotVel, double dist) {
        return hubPosition.minus(
            new Translation2d(robotVel.vxMetersPerSecond, robotVel.vyMetersPerSecond).times(getAirtime(dist)));
    }

    public static double getAirtime(double dist) {
        return 0.0;// 0.1*dist + 0.73;
    }

    public static Translation2d getAdjHubSimple(CommandSwerveDrivetrain drivetrain) {
        return ShotPredictor.getAdjustedHub(ChassisSpeeds.fromRobotRelativeSpeeds(
            drivetrain.getState().Speeds,
            drivetrain.getState().Pose.getRotation()),
            drivetrain.getPose().getTranslation().getDistance(ShotPredictor.hubPosition));
    }
}