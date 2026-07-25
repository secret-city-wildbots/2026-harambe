package frc.robot.Actors.Subsystems.Shooter;

import org.ironmaple.simulation.SimulatedArena;
import org.ironmaple.simulation.drivesims.AbstractDriveTrainSimulation;
import org.ironmaple.simulation.seasonspecific.rebuilt2026.RebuiltFuelOnFly;

import dev.doglog.DogLog;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.Utils.ShotPredictor;

public class ShooterSim implements Shooter {
    private Timer lastShot = new Timer();
    private AbstractDriveTrainSimulation drivetrain;

    public ShooterSim(AbstractDriveTrainSimulation drivetrain) {
        this.drivetrain = drivetrain;
    }

    public void startShooting() {
        lastShot.start();
    }

    public void stop() {
        lastShot.stop();
    }

    public void periodic() {
        if (lastShot.get() > 0.2) {
            lastShot.reset();
            SimulatedArena.getInstance()
                    .addGamePieceProjectile(new RebuiltFuelOnFly(
                            drivetrain.getSimulatedDriveTrainPose().getTranslation(),
                            new Translation2d(-0.14,-0.03), // shooter offet from center
                            drivetrain.getDriveTrainSimulatedChassisSpeedsFieldRelative(),
                            drivetrain.getSimulatedDriveTrainPose().getRotation().plus(new Rotation2d(-Math.PI/2)),
                            Units.Meters.of(0.4), // initial height of the ball, in meters
                            Units.MetersPerSecond.of(ShotPredictor.getShotVel(drivetrain.getSimulatedDriveTrainPose().getTranslation())), // initial velocity, in m/s
                            Units.Degrees.of(62)) // shooter angle
                            .withProjectileTrajectoryDisplayCallBack(
                                    (poses) -> DogLog.log("Simulation/successfulShotsTrajectory",
                                            poses.toArray(Pose3d[]::new)),
                                    (poses) -> DogLog.log("Simulation/missedShotsTrajectory",
                                            poses.toArray(Pose3d[]::new)))
                            .withHitTargetCallBack(() -> System.out.println("Hit hub, +1 point!"))
                            .enableBecomesGamePieceOnFieldAfterTouchGround());
        }
    }
}
