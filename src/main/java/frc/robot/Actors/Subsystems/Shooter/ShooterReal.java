package frc.robot.Actors.Subsystems.Shooter;

import frc.robot.Actors.Motor;

import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.signals.MotorAlignmentValue;

import frc.robot.Constants.*;
import frc.robot.Utils.MotorType;
import frc.robot.Utils.RotationDir;

public class ShooterReal implements Shooter {
    private final Motor leadMotor;
    private final Motor followerMotor;

    public ShooterReal() {
        this.leadMotor = new Motor(ShooterConstants.leadMotorID, MotorType.TFX);
        this.followerMotor = new Motor(ShooterConstants.followerMotorID, MotorType.TFX);

        this.leadMotor.motorConfig.direction = RotationDir.CounterClockwise;
        this.leadMotor.motorConfig.peakReverseDC = 0.0;
        this.leadMotor.motorConfig.brake = false;
        this.leadMotor.applyConfig();
        this.leadMotor.slot0TFX.kV = 0.11;
        this.leadMotor.pid(0.4, 0.0, 0.0); // Setup the Shooter PID

        this.followerMotor.motorTFX.setControl(new Follower(ShooterConstants.leadMotorID, MotorAlignmentValue.Opposed));
    }

    public void startShooting() {

    }

    public void stop() {

    }

    public void periodic() {
        
    }
}
