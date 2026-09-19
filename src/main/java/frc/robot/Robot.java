// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.Utils;
import dev.doglog.DogLog;
import dev.doglog.DogLogOptions;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Actors.Vision;
import frc.robot.Utils.LimelightHelpers;
import frc.robot.Utils.simulation.FixedArena2026Rebuilt;
import frc.robot.Utils.simulation.FuelBumpSim;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.util.Units;

import org.ironmaple.simulation.SimulatedArena;

public class Robot extends TimedRobot {
    private Command m_autonomousCommand;

    private final RobotContainer m_robotContainer;

    private final boolean kUseLimelight = false;

    private final Vision vision;

    private final FuelBumpSim fuelBumpSim = new FuelBumpSim();

    public static boolean test = false;

    public static boolean dummyMode = false;

    public Robot() {
        if (RobotBase.isSimulation()) {
            SimulatedArena.overrideInstance(new FixedArena2026Rebuilt(false));
            SimulatedArena.getInstance().resetFieldForAuto();
        }
        m_robotContainer = new RobotContainer();

        m_robotContainer.drivetrain.getPigeon2().reset();

        // Setup vision with the suppliers from the drivetrain (heading and rotation
        // (rps))
        // This allows each limelight to be as accurate as possible when being setup
        vision = new Vision(
            () -> m_robotContainer.drivetrain.getState().Pose.getRotation().getDegrees(),
            () -> Units.radiansToRotations(m_robotContainer.drivetrain.getState().Speeds.omegaRadiansPerSecond),
            () -> m_robotContainer.drivetrain.getPose(),
            () -> m_robotContainer.drivetrain.getPigeon2().getRotation2d());

    }

    @Override
    public void robotInit() {
        DogLog.setOptions(new DogLogOptions()
            .withLogExtras(true)
            .withCaptureDs(true)
            .withNtPublish(true)
            .withCaptureNt(true));
        DogLog.setPdh(new PowerDistribution());
    }

    @Override
    public void robotPeriodic() {
        m_robotContainer.intake.periodic();
        CommandScheduler.getInstance().run();

        m_robotContainer.shooter.periodic();

        /*
         * This example of adding Limelight is very simple and may not be sufficient for
         * on-field use.
         * Users typically need to provide a standard deviation that scales with the
         * distance to target
         * and changes with number of tags available.
         *
         * This example is sufficient to show that vision integration is possible,
         * though exact implementation
         * of how to use vision should be tuned per-robot and to the team's
         * specification.
         */
        if (kUseLimelight) {
            var llMeasurement = LimelightHelpers.getBotPoseEstimate_wpiBlue("limelight");
            if (llMeasurement != null) {
                m_robotContainer.drivetrain.addVisionMeasurement(
                    llMeasurement.pose, Utils.fpgaToCurrentTime(llMeasurement.timestampSeconds));
            }
        }

        m_robotContainer.dashboard.update();

        m_robotContainer.elevator.periodic();

        LimelightHelpers.PoseEstimate bestPose = vision.getBestPose();
        //Vision.FusedVisionResult fusedPose = vision.fuseFourLimelights();

        // If bestPose is not null, add vision measurement to the drivetrain
        // TODO: need to tune 0.7,0.7 values
        /*
         * LimelightHelpers.PoseEstimate[] poses = vision.getPoses();
         * for (LimelightHelpers.PoseEstimate pose: poses) {
         * m_robotContainer.drivetrain.addVisionMeasurement(pose.pose,
         * pose.timestampSeconds,
         * VecBuilder.fill(vision.getStdDev(pose),vision.getStdDev(pose),9999999));
         * }
         */
        if (bestPose != null) {
            // TODO: Do we want to just only add or reset the whole pose?
            m_robotContainer.drivetrain.addVisionMeasurement(bestPose.pose, bestPose.timestampSeconds,
                VecBuilder.fill(0.7, 0.7, 9999999));
            //m_robotContainer.drivetrain.addVisionMeasurement(fusedPose.pose(), fusedPose.tiemstamp(), VecBuilder.fill(0.7,0.7,9999999));
            //m_robotContainer.drivetrain.resetPose(bestPose.pose);
        }
    }

    @Override
    public void disabledInit() {
    }

    @Override
    public void disabledPeriodic() {
        //System.out.println(LimelightHelpers.getHeartbeat("limelight-right"));
        LimelightHelpers.PoseEstimate LLRightPose = LimelightHelpers.getBotPoseEstimate_wpiBlue("limelight-right");

        // If the pose is not null and it sees an april tag
        if (LLRightPose != null && LLRightPose.tagCount > 0) {
            // Reset the robots rotation and pose directly
            m_robotContainer.drivetrain.resetRotation(LLRightPose.pose.getRotation());
            m_robotContainer.drivetrain.resetPose(LLRightPose.pose);
            System.out.println("RE-ZEROED IMU");
        } else {
            //System.out.println("t" + LimelightHelpers.getHeartbeat("limelight-right"));
        }
    }

    @Override
    public void disabledExit() {
    }

    @Override
    public void autonomousInit() {
        m_autonomousCommand = m_robotContainer.getAutonomousCommand();

        if (m_autonomousCommand != null) {
            m_autonomousCommand.schedule();
        }
    }

    @Override
    public void autonomousPeriodic() {
    }

    @Override
    public void autonomousExit() {
    }

    @Override
    public void teleopInit() {
        if (m_autonomousCommand != null) {
            m_autonomousCommand.cancel();
        }
    }

    @Override
    public void teleopPeriodic() {
    }

    @Override
    public void teleopExit() {
    }

    @Override
    public void testInit() {
        CommandScheduler.getInstance().cancelAll();
    }

    @Override
    public void testPeriodic() {
    }

    @Override
    public void testExit() {
    }

    @Override
    public void simulationPeriodic() {
        DogLog.log("Simulation/FuelPoses", fuelBumpSim.update(5));
    }
}