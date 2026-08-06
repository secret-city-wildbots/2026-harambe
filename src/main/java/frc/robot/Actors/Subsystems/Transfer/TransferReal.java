package frc.robot.Actors.Subsystems.Transfer;

import frc.robot.Actors.Motor;
import frc.robot.Utils.MotorType;
import frc.robot.Constants.*;

public class TransferReal implements Transfer {
    private final Motor motor;

    public TransferReal() {
        this.motor = new Motor(TransferConstants.motorID, MotorType.TFX);
        this.motor.setBrake(false);

        this.motor.slot0TFX.kV = 0.12;
        this.motor.pid(0.01, 0, 0);
    }

    public void startShooting() {
        this.motor.vel(60);
    }

    public void stop() {
        this.motor.volt(0);
    }
}