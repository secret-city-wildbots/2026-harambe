package frc.robot.Actors.Subsystems.Shooter;

import frc.robot.Actors.Motor;
import frc.robot.Actors.Subsystems.CommandSwerveDrivetrain;

import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.signals.MotorAlignmentValue;

import frc.robot.Constants.*;
import frc.robot.Utils.MotorType;
import frc.robot.Utils.RotationDir;
import frc.robot.Utils.ShotPredictor;

public class ShooterReal implements Shooter {
    private final Motor leadMotor;
    private final Motor followerMotor;
    private final CommandSwerveDrivetrain drivetrain;

    public ShooterReal(CommandSwerveDrivetrain drivetrain) {
        this.leadMotor = new Motor(ShooterConstants.leadMotorID, MotorType.TFX);
        this.followerMotor = new Motor(ShooterConstants.followerMotorID, MotorType.TFX);

        this.leadMotor.motorConfig.direction = RotationDir.CounterClockwise;
        this.leadMotor.motorConfig.peakReverseDC = 0.0;
        this.leadMotor.motorConfig.brake = false;
        this.leadMotor.applyConfig();
        this.leadMotor.slot0TFX.kV = 0.12;
        this.leadMotor.pid(0.03, 0.0, 0.0); // Setup the Shooter PID

        this.drivetrain = drivetrain;

        this.followerMotor.motorTFX.setControl(new Follower(ShooterConstants.leadMotorID, MotorAlignmentValue.Opposed));
    }

    public void startShooting() {
        this.leadMotor.vel(40);//ShotPredictor.getShotRPS(drivetrain.getPose().getTranslation()));
    }

    public void stop() {
        this.leadMotor.dc(0);
    }

    public void periodic() {
        
    }
}
