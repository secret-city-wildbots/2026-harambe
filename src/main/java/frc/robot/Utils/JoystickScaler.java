package frc.robot.Utils;

public class JoystickScaler {
    /**
     * Scale joystick input for driving
     * @param input [-1,1]
     * @return [-1,1]
     */
    public static double scaleStrafe(double input) {
        return Math.signum(input)*(Math.abs(input) > 0.1 ? Math.pow(Math.abs(input),1.5):0.0);
    }

    public static double scaleRotate(double input) {
        return Math.signum(input)*(Math.abs(input) > 0.1 ? Math.pow(Math.abs(input),1.5):0.0);
    }
}