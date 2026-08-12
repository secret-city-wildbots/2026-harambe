package frc.robot.Actors.Subsystems.Indexer;

import frc.robot.Actors.Motor;
import frc.robot.Constants.*;
import frc.robot.Utils.MotorType;
import frc.robot.Utils.RotationDir;

public class IndexerReal implements Indexer {
    private final Motor indexerMotor;

    public IndexerReal() {
        this.indexerMotor = new Motor(IndexerConstants.indexerMotorID, MotorType.TFX);

        this.indexerMotor.motorConfig.direction = RotationDir.CounterClockwise;
        this.indexerMotor.motorConfig.brake = false;
        this.indexerMotor.applyConfig();

        this.indexerMotor.slot0TFX.kV = 0.11;
        this.indexerMotor.pid(0.01, 0, 0);
    }

    public void startIndexer() {
        indexerMotor.vel(50);
    }

    public void startReversing() {
        indexerMotor.vel(-30);
    }

    public void stop() {
        indexerMotor.volt(0);
    }
}