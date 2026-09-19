// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import static edu.wpi.first.units.Units.*;

import org.ironmaple.simulation.drivesims.AbstractDriveTrainSimulation;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.pathfinding.Pathfinder;
import com.pathplanner.lib.events.EventTrigger;
import com.pathplanner.lib.path.PathConstraints;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.generated.TunerConstants;
import frc.robot.Actors.Subsystems.CommandSwerveDrivetrain;
import frc.robot.Actors.Subsystems.Elevator.Elevator;
import frc.robot.Actors.Subsystems.Elevator.ElevatorDummy;
import frc.robot.Actors.Subsystems.Elevator.ElevatorReal;
import frc.robot.Actors.Subsystems.Elevator.ElevatorSim;
import frc.robot.Actors.Subsystems.Indexer.Indexer;
import frc.robot.Actors.Subsystems.Indexer.IndexerDummy;
import frc.robot.Actors.Subsystems.Indexer.IndexerReal;
import frc.robot.Actors.Subsystems.Indexer.IndexerSim;
import frc.robot.Actors.Subsystems.Intake.Intake;
import frc.robot.Actors.Subsystems.Intake.IntakeDummy;
import frc.robot.Actors.Subsystems.Intake.IntakeReal;
import frc.robot.Actors.Subsystems.Intake.IntakeSim;
import frc.robot.Actors.Subsystems.Shooter.Shooter;
import frc.robot.Actors.Subsystems.Shooter.ShooterDummy;
import frc.robot.Actors.Subsystems.Shooter.ShooterReal;
import frc.robot.Actors.Subsystems.Shooter.ShooterSim;
import frc.robot.Actors.Subsystems.Transfer.Transfer;
import frc.robot.Actors.Subsystems.Transfer.TransferDummy;
import frc.robot.Actors.Subsystems.Transfer.TransferReal;
import frc.robot.Actors.Subsystems.Transfer.TransferSim;
import frc.robot.Commands.Subsystems.Drivetrain.AimAtHeadingAssist;
import frc.robot.Commands.Subsystems.Drivetrain.AutoAlignToClimb;
import frc.robot.Commands.Subsystems.Elevator.ClimbSequenceL1;
import frc.robot.Commands.Subsystems.Elevator.ClimbSequenceL3;
import frc.robot.Commands.Subsystems.Elevator.ExtendLiftCommand;
import frc.robot.Commands.Subsystems.Elevator.RetractLiftCommand;
import frc.robot.Commands.Subsystems.Elevator.RotateHookToPositionCommand;
import frc.robot.Commands.Subsystems.Shooter.Shoot;
import frc.robot.Commands.Subsystems.Shooter.SimpleShoot;
import frc.robot.Utils.ShotPredictor;
import frc.robot.Utils.JoystickScaler;

public class RobotContainer {
    public static double MaxSpeed = TunerConstants.kSpeedAt12Volts.in(MetersPerSecond); // kSpeedAt12Volts desired
                                                                                       // top
                                                                                       // speed
    public static double MaxAngularRate = RotationsPerSecond.of(0.75).in(RadiansPerSecond); // 3/4 of a rotation per
                                                                                           // second max angular
                                                                                           // velocity

    /* Setting up bindings for necessary control of the swerve drive platform */
    public static SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
        .withDeadband(MaxSpeed)
        .withRotationalDeadband(MaxAngularRate) // Add a 10% deadband
        .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive motors
    private final SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();
    private final SwerveRequest.PointWheelsAt point = new SwerveRequest.PointWheelsAt();
    private final SwerveRequest.RobotCentric forwardStraight = new SwerveRequest.RobotCentric()
        .withDriveRequestType(DriveRequestType.OpenLoopVoltage);

    private final Telemetry logger = new Telemetry(MaxSpeed);

    private final CommandXboxController joystick = new CommandXboxController(0);

    public final Intake intake;

    public final Shooter shooter;

    public final Transfer transfer;

    public final Indexer indexer;

    public final Elevator elevator;

    public final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();

    public final Dashboard dashboard;

    public boolean speedDown = false;

    // public final Intake intake;

    public RobotContainer() {

        /*
         * if (RobotBase.isSimulation()) {
         * intake = new IntakeSim(drivetrain.get);
         * } else {
         * intake = new IntakeReal();
         * }
         */
        if (Robot.dummyMode) {
            intake = new IntakeDummy();
            shooter = new ShooterDummy();
            transfer = new TransferDummy();
            indexer = new IndexerDummy();
            elevator = new ElevatorDummy();
        } else if (RobotBase.isReal()) {
            intake = new IntakeReal();
            shooter = new ShooterReal(drivetrain);
            transfer = new TransferReal();
            indexer = new IndexerReal();
            elevator = new ElevatorReal();
        } else {
            intake = new IntakeSim(drivetrain.getDriveSimulation());
            shooter = new ShooterSim(drivetrain.getDriveSimulation());
            transfer = new TransferSim();
            indexer = new IndexerSim();
            elevator = new ElevatorSim();
        }

        new EventTrigger("Intake").toggleOnTrue(Commands.runEnd(intake::startIntaking, intake::stop, intake));
        new EventTrigger("Shoot").toggleOnTrue(new Shoot(shooter, indexer, transfer));
        new EventTrigger("ClimbL1").toggleOnTrue(new ClimbSequenceL1(elevator));

        configureBindings();

        drivetrain.resetPose(new Pose2d(3, 3, new Rotation2d()));

        dashboard = new Dashboard(drivetrain, shooter, indexer, transfer, intake, null);
    }

    /** Auto armed from the dashboard's Autos tab, or null if none. */
    private Command armedAuto = null;

    private void configureBindings() {

        // Descend from Auto L1 + Retract Lift down

        // joystick.y().whileTrue(new ExtendLiftCommand(elevator, .35));
        // joystick.a().whileTrue(new RetractLiftCommand(elevator, false, .5));
        // joystick.x().whileTrue(new ClimbSequenceL3(elevator));
        //joystick.b().toggleOnTrue(new ClimbSequenceL1(elevator));
        joystick.x().whileTrue(new ExtendLiftCommand(elevator, .35));
        joystick.b().whileTrue(new RetractLiftCommand(elevator, true, .35));
        joystick.y().onTrue(Commands.runOnce(() -> {
            speedDown = !speedDown;
        }));

        //joystick.pov(180).whileTrue(new RotateHookToPositionCommand(elevator, 0.1));

        //joystick.pov(90).whileTrue(new AutoAlignToClimb());

        ShotPredictor.getAdjustedHub(ChassisSpeeds.fromRobotRelativeSpeeds(
            drivetrain.getState().Speeds,
            drivetrain.getState().Pose.getRotation()),
            drivetrain.getPose().getTranslation().getDistance(ShotPredictor.hubPosition));

        joystick.leftBumper().toggleOnTrue(Commands.runEnd(intake::startIntaking, intake::stop, intake));
        joystick.leftTrigger(0.4).whileTrue(new Shoot(shooter, indexer, transfer));
        joystick.rightTrigger(0.4).whileTrue(Commands.runEnd(
            () -> {
                shooter.startShooting();
                indexer.startIndexer();
                transfer.startShooting();
            },
            () -> {
                shooter.stop();
                indexer.stop();
                transfer.stop();
            },
            shooter, indexer, transfer));

        joystick.a().whileTrue(new AimAtHeadingAssist(drivetrain, () -> {
            return ShotPredictor.getAdjHubSimple(drivetrain).minus(drivetrain.getPose().getTranslation()).getAngle()
                .plus(new Rotation2d(Math.PI / 2));
        }, () -> {
            return (-joystick.getLeftY() * MaxSpeed * 0.5);
        }, () -> {
            return (-joystick.getLeftX() * MaxSpeed * 0.5);
        }));

        joystick.rightBumper().whileTrue(new SimpleShoot(shooter, indexer, transfer, 55));

        // Note that X is defined as forward according to WPILib convention,
        // and Y is defined as to the left according to WPILib convention.
        if (RobotBase.isReal()) {
            drivetrain.setDefaultCommand(
                // Drivetrain will execute this command periodically
                drivetrain.applyRequest(
                    () -> drive.withVelocityX(
                        -joystick.getLeftY() * MaxSpeed * (speedDown ? 0.2 : 1)) // Drive forward with negative Y (forward)
                        .withVelocityY(-joystick.getLeftX() * MaxSpeed * (speedDown ? 0.2 : 1)) // Drive left with negative X (left)
                        .withRotationalRate(-joystick.getRightX()
                            * MaxAngularRate * (speedDown ? 0.3 : 1)) // Drive counterclockwise with negative X (left)
                ));
        } else {
            drivetrain.setDefaultCommand(
                // Drivetrain will execute this command periodically
                drivetrain.applyRequest(
                    () -> drive.withVelocityX(
                        -(joystick.getLeftY()) * (MaxSpeed + ((Math.random() - 0.5) * 0.5))) // Drive forward with negative Y (forward)
                        .withVelocityY(-(joystick.getLeftX()) * (MaxSpeed + ((Math.random() - 0.5) * 0.5))) // Drive left with negative X (left)
                        .withRotationalRate(-joystick.getRightX()
                            * MaxAngularRate * 1.5) // Drive counterclockwise with negative X (left)
                ));
        }

        /*
         * joystick.a().whileTrue(drivetrain.applyRequest(() -> brake));
         * joystick.b()
         * .whileTrue(drivetrain.applyRequest(
         * () -> point.withModuleDirection(new Rotation2d(-joystick.getLeftY(),
         * -joystick.getLeftX()))));
         */

        // joystick.pov(0)
        // .whileTrue(drivetrain.applyRequest(
        // () -> forwardStraight.withVelocityX(0.5).withVelocityY(0)));
        // joystick.pov(180)
        // .whileTrue(drivetrain.applyRequest(
        // () -> forwardStraight.withVelocityX(-0.5).withVelocityY(0)));

        // Run SysId routines when holding back/start and X/Y.
        // Note that each routine should be run exactly once in a single log.
        // joystick.back().and(joystick.y()).whileTrue(drivetrain.sysIdDynamic(Direction.kForward));
        // joystick.back().and(joystick.x()).whileTrue(drivetrain.sysIdDynamic(Direction.kReverse));
        // joystick.start().and(joystick.y()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kForward));
        // joystick.start().and(joystick.x()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kReverse));

        // reset the field-centric heading on left pov
        joystick.pov(270).onTrue(drivetrain.runOnce(() -> drivetrain.seedFieldCentric()));

        drivetrain.registerTelemetry(logger::telemeterize);
    }

    public Command getAutonomousCommand() {
        /* Run the auto armed in the dashboard's "Autos" tab */
        if (armedAuto == null) {
            DriverStation.reportWarning(
                "No auto armed — pick one in the dashboard's Autos tab", false);
            return Commands.none();
        }
        return armedAuto;
    }
}