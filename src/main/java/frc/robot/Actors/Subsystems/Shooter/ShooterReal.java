package frc.robot.Actors.Subsystems.Shooter;

import frc.robot.Actors.Motor;
import frc.robot.Actors.Subsystems.CommandSwerveDrivetrain;

import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.signals.MotorAlignmentValue;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
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
        this.leadMotor.slot0TFX.kV = 0.13;
        this.leadMotor.pid(0.1, 0.0, 0.0); // Setup the Shooter PID

        this.drivetrain = drivetrain;

        this.followerMotor.motorTFX.setControl(new Follower(ShooterConstants.leadMotorID, MotorAlignmentValue.Opposed));
    }

    public void startShooting() {
        this.leadMotor.vel(ShotPredictor.getShotRPS(drivetrain.getPose().getTranslation(),
            ChassisSpeeds.fromRobotRelativeSpeeds(
                drivetrain.getState().Speeds,
                drivetrain.getState().Pose.getRotation())));
    }

    public void shootOverride(double vel) {
        this.leadMotor.vel(vel);
    }

    public void stop() {
        this.leadMotor.dc(0);
    }

    public void periodic() {
        //System.out.println(drivetrain.getPose().getTranslation().getDistance(ShotPredictor.hubPosition));
    }
}
