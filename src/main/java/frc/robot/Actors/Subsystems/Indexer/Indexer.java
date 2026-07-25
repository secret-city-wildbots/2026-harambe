package frc.robot.Actors.Subsystems.Indexer;

import edu.wpi.first.wpilibj2.command.Subsystem;

public interface Indexer extends Subsystem {
    public void startIndexer();
    public void startReversing();
    public void stop();
}