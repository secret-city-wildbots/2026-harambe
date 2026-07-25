package frc.robot.Actors.Subsystems.Intake;

import static edu.wpi.first.units.Units.Meters;

import org.ironmaple.simulation.IntakeSimulation;
import org.ironmaple.simulation.drivesims.AbstractDriveTrainSimulation;

import dev.doglog.DogLog;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;

public class IntakeSim implements Intake {
    private final IntakeSimulation intakeSimulation;

    public IntakeSim(AbstractDriveTrainSimulation drivetrain) {
        this.intakeSimulation = IntakeSimulation.OverTheBumperIntake(
            // Specify the type of game pieces that the intake can collect
            "Fuel",
            // Specify the drivetrain to which this intake is attached
            drivetrain,
            // Width of the intake
            Meters.of(0.7),
            // The extension length of the intake beyond the robot's frame (when activated)
            Meters.of(0.2),
            // The intake is mounted on the front side of the chassis
            IntakeSimulation.IntakeSide.FRONT,
            // The intake can hold up to 50 fuel
            50);

        DogLog.log("Simulation/Components/IntakePose3d", new Pose3d(0.297,0,0.196, new Rotation3d(0, 0, 0)));
    }

    public void startIntaking() {
        this.intakeSimulation.startIntake();
        DogLog.log("Simulation/Components/IntakePose3d", new Pose3d(0.297,0,0.196, new Rotation3d(0, Math.PI/2, 0)));
    }

    public void startOuttaking() {
    }

    public void stop() {
        intakeSimulation.setGamePiecesCount(0);
        this.intakeSimulation.stopIntake();
        DogLog.log("Simulation/Components/IntakePose3d", new Pose3d(0.297,0,0.196, new Rotation3d(0, 0, 0)));
    }

    public void startIntakeVoltage(double voltage) {
        this.intakeSimulation.startIntake();
        DogLog.log("Simulation/Components/IntakePose3d", new Pose3d(0.297,0,0.196, new Rotation3d(0, Math.PI/2, 0)));
     }
}
