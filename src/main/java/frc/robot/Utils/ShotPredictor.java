package frc.robot.Utils;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

public class ShotPredictor {
    public static double hubX = (DriverStation.getAlliance().get() == Alliance.Blue) ? (4.63):(11.9);
    public static Translation2d hubPosition = new Translation2d(hubX, 4.035);

    public static double getShotVel(Translation2d pose) {
        return pose.getDistance(hubPosition)*1.3 + 3.5;
    }
}
