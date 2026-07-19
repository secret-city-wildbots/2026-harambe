package frc.robot.Actors.Subsystems.Intake;

import frc.robot.Actors.Motor;
import frc.robot.Constants.IntakeConstants;
import frc.robot.Utils.MotorType;

public class IntakeReal implements Intake {
    private final Motor intakeMotor;
    private final Motor extensionMotor;

    public IntakeReal() {
        this.intakeMotor = new Motor(IntakeConstants.intakeMotorID, MotorType.TFX);
        this.extensionMotor = new Motor(IntakeConstants.extensionMotorID, MotorType.TFX);

        this.intakeMotor.configTFX.Slot0.kV = 0.3;
        this.intakeMotor.pid(0, 0, 0);

        this.extensionMotor.pid(0.5, 0, 0);
    }

    public void startIntaking() {
        extensionMotor.pos(IntakeConstants.maxDegree);
        intakeMotor.vel(40);
    }

    public void startOuttaking() {
        extensionMotor.pos(IntakeConstants.maxDegree);
        intakeMotor.vel(-30);
    }

    public void stop() {
        extensionMotor.pos(IntakeConstants.minDegree);
        intakeMotor.volt(0);
    }

    public void startIntakeVoltage(double voltage) {
        extensionMotor.pos(IntakeConstants.maxDegree);
        intakeMotor.volt(voltage);
    }
}
