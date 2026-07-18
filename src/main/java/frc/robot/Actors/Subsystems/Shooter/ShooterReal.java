package frc.robot.Actors.Subsystems.Shooter;

import frc.robot.Actors.Motor;
import frc.robot.Constants.*;
import frc.robot.Utils.MotorType;

public class ShooterReal implements Shooter {
    private final Motor leadMotor;

    public ShooterReal() {
        this.leadMotor = new Motor(ShooterConstants.leadMotorID, MotorType.TFX);

    }

    public void startShooting() {

    }

    public void stop() {

    }

    public void periodic() {
        
    }
}
