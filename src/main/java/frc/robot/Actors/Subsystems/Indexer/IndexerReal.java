package frc.robot.Actors.Subsystems.Indexer;

import frc.robot.Actors.Motor;
import frc.robot.Constants.*;
import frc.robot.Utils.MotorType;

public class IndexerReal implements Indexer {
    private final Motor indexerMotor;

    public IndexerReal() {
        this.indexerMotor = new Motor(IndexerConstants.indexerMotorID, MotorType.TFX);

        this.indexerMotor.configTFX.Slot0.kV = 0.3;
        this.indexerMotor.pid(0, 0, 0);
    }

    public void startIndexer() {
        indexerMotor.vel(40);
    }

    public void startReversing() {
        indexerMotor.vel(-30);
    }

    public void stop() {
        indexerMotor.volt(0);
    }
}