package frc.robot.Actors.Subsystems.Intake;

import frc.robot.Actors.Motor;
import frc.robot.Constants.IntakeConstants;
import frc.robot.Utils.MotorType;

public class IntakeReal implements Intake {
    private final Motor intakeMotor;

    public IntakeReal() {
        this.intakeMotor = new Motor(IntakeConstants.intakeMotorID, MotorType.TFX);

    }

    public void startIntaking() {

    }

    public void startOuttaking() {

    }

    public void stop() {

    }

    public void startIntakeVoltage(double voltage) {

    }
}
