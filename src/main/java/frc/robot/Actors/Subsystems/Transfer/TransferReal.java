package frc.robot.Actors.Subsystems.Transfer;

import frc.robot.Actors.Motor;
import frc.robot.Utils.MotorType;
import frc.robot.Constants.*;

public class TransferReal implements Transfer {
    private final Motor motor;

    public TransferReal() {
        this.motor = new Motor(TransferConstants.motorID, MotorType.TFX);
        this.motor.configTFX.Slot0.kV = 0.01;
        this.motor.pid(0, 0, 0);
    }

    public void startShooting() {
        this.motor.vel(60);
    }

    public void stop() {
        this.motor.dc(0);
    }
}