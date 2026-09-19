package frc.robot.Utils;

import com.ctre.phoenix6.swerve.SwerveDrivetrain.SwerveDriveState;

public class Slopify {
    public static double slopify(double input, SwerveDriveState state) {
        return (Math.abs(input) > 0.2 || state.Speeds.vxMetersPerSecond > 0.1 || state.Speeds.vyMetersPerSecond > 0.1)
            ? input
            : 0;
    }
}
