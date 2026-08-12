package frc.robot.Utils;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

public class ShotPredictor {
    public static double hubX = (DriverStation.getAlliance().get() == Alliance.Blue) ? (4.63):(11.9);
    public static Translation2d hubPosition = new Translation2d(hubX, 4.035);

    public static double getShotVel(Translation2d pose) {
        double dist = pose.getDistance(hubPosition);
        System.out.println(dist);

        //return SmartDashboard.getNumber("pow", 0.0);
        return 6.6+(dist-1.68)*(0.392) + ((dist > 2.4) ? (dist-2.4)*0.35:0);
    }

    public static double getShotRPS(Translation2d pose) {
        double dist = pose.getDistance(hubPosition);
        return 1.0;
    }
}