package frc.robot.Actors.Subsystems.Intake;

import frc.robot.Robot;
import frc.robot.Actors.Motor;
import frc.robot.Constants.IntakeConstants;
import frc.robot.Utils.MotorType;

public class IntakeReal implements Intake {
    private final Motor intakeMotor;
    private final Motor extensionMotor;

    public IntakeReal() {
        this.intakeMotor = new Motor(IntakeConstants.intakeMotorID, MotorType.TFX);
        this.extensionMotor = new Motor(IntakeConstants.extensionMotorID, MotorType.TFX);

        this.extensionMotor.applyConfig();
        this.extensionMotor.motionMagic(4, 0, 0, 0, 0, 90, 13);

        this.intakeMotor.configTFX.Slot0.kV = 0.14;
        this.intakeMotor.pid(0.05, 0, 0);
    }

    public void startIntaking() {
        extensionMotor.posMM(IntakeConstants.maxRotations);
        intakeMotor.vel(80);
        //intakeMotor.volt(12);
    }

    public void startOuttaking() {
        extensionMotor.posMM(IntakeConstants.maxRotations);
        intakeMotor.vel(-30);
    }

    public void stop() {
        extensionMotor.posMM(IntakeConstants.minRotations);
        intakeMotor.vel(0);
    }

    public void startIntakeVoltage(double voltage) {
        extensionMotor.posMM(IntakeConstants.maxRotations);
        intakeMotor.volt(voltage);
    }
}
