package frc.robot.Commands.Subsystems.Shooter;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandGenericHID;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

import frc.robot.Actors.Subsystems.Indexer.Indexer;
import frc.robot.Actors.Subsystems.Shooter.Shooter;
import frc.robot.Actors.Subsystems.Transfer.Transfer;

public class Shoot extends ParallelCommandGroup {
    public Shoot(Shooter shooter, Indexer indexer, Transfer transfer) {

        addCommands(

                Commands.runEnd(
                    () -> {shooter.startShooting(); transfer.startShooting();},
                    () -> {shooter.stop(); transfer.stop();},
                    shooter, transfer
                ),
            
                new SequentialCommandGroup(
                    Commands.waitSeconds(1),

                    Commands.runEnd(indexer::startIndexer, indexer::stop, indexer)
                )
        );
    }
}

