package frc.robot;

import java.util.function.Consumer;

import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.swerve.SwerveModule;
import com.pathplanner.lib.commands.PathPlannerAuto;

import edu.wpi.first.networktables.NetworkTableInstance; 
import edu.wpi.first.hal.can.CANStatus;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Actors.Subsystems.CommandSwerveDrivetrain;
import frc.robot.Actors.Subsystems.Intake.Intake;
import frc.robot.Actors.Subsystems.Shooter.Shooter;
import frc.robot.Actors.Subsystems.Transfer.Transfer;
import frc.robot.Actors.Subsystems.Indexer.Indexer;
import frc.robot.WildBoard.WildBoard;
import frc.robot.WildBoard.Panels.*;


public class Dashboard {
    public WildBoard dashboard;

    private CommandSwerveDrivetrain drivetrain;
    private Shooter shooter;
    private Indexer indexer;
    private Transfer transfer;
    private Intake intake;

    final VelocitySimpleSubsystem WBshooter;
    final SimpleSubsystem WBintake;
    final VelocitySimpleSubsystem WBindexer;
    final VelocitySimpleSubsystem WBtransfer;
    final SwerveModules WBswerveModules;
    final MasterAlarms WBalarms;
    final FieldMap WBfieldMap;
    final NumberDisplay WBnumberDisplay;
    final AutoTools WBautoTools;
    final AutoChooser WBautoChooser;

    public double battAvg = 12.0;
    public double currAvg = 50.0;
    
    public boolean shotSmoothing = true;

    private Consumer<Command> autoChosen;

    public Dashboard(CommandSwerveDrivetrain drivetrain, Shooter shooter, Indexer indexer, Transfer transfer, Intake intake, Consumer<Command> autoChoosen) {
        this.drivetrain = drivetrain;
        this.shooter = shooter;
        this.indexer = indexer;
        this.transfer = transfer;
        this.intake = intake;
        this.autoChosen = autoChoosen;
        dashboard = new WildBoard(5804);

        WBfieldMap = new FieldMap();
        WBautoChooser = new AutoChooser();

        // Checklist
        dashboard.addTab(new Tab()
                .addChild(new Checklist())
                .setTitle("Checklist"));

        // TeleOp
        dashboard.addTab(new Tab()
                .setTitle("TeleOp")
                .addChild(new Col(2).addChild(
                        WBfieldMap))
                .addChild(new Col(6).addChild(
                        new Row().addChild(
                                new CameraFeed(11)).addChild( //?
                                        new CameraFeed(12)))
                        .addChild(
                                new Row().addChild(
                                        new CameraFeed(13)).addChild(
                                                new CameraFeed(14))))
                .addChild(new Col(4).addChild(
                        // Display only. Autos are armed from the "Autos" tab,
                        // which validates them before letting you pick one.
                        WBautoChooser).addChild(
                                new Overrides(new String[] { "Limelight PowerSaver", "Disable Camera Feeds", "CompMode",
                                        "Disable Shot Smoothing", "Always Aim at Hub", "Disable Shoot Safeties" },
                                        2).onChange((Integer id, Boolean state) -> {
                                            // TODO: Check if this actually overrides anything
                                            if (id == 3) { //shot smoothing
                                                this.shotSmoothing = !state;
                                            }
                                            if (id == 1) {
                                                /*for (String ll : VisionConstants.limelightNames) {
                                                    NetworkTableInstance.getDefault()
                                                        .getTable(ll)
                                                        .getEntry("camMode")
                                                        .setNumber(state ? 1 : 0);
                                                }*/
                                            }
                                        }))));

        // Subsystems
        WBshooter = new VelocitySimpleSubsystem("Shooter");
        WBintake = new SimpleSubsystem("Intake", true, "rps");
        WBtransfer = new VelocitySimpleSubsystem("Transfer");
        WBindexer = new VelocitySimpleSubsystem("Indexer");
        WBnumberDisplay = new NumberDisplay("BPS: ");

        WBswerveModules = new SwerveModules();
        WBalarms = new MasterAlarms(
                new String[] { "SWOH", "SSOH", "CNBS", "CURR", "JOYS", "PING", "LOOP", "BATT", "blank", "blank" },
                new String[] { "Swerve Overheat", "Subsystem Overheat", "Canbus Error", "Current High",
                        "Joystick Disconnect", "Ping High/Failed", "Loop Time too High", "Battery Voltage Low",
                    "Placeholder for future issues", "Placeholder for future issues" },
                2);

        dashboard.addTab(new Tab()
                .addChild(
                        new Col(4).addChild(
                                WBswerveModules).addChild(
                                        new SystemsCheck().onTest(() -> {
                                            System.out.println("running SysCheck");
                                        })))
                .addChild(
                        new Col(3).addChild(
                            WBnumberDisplay
                        ))
                .addChild(
                        new Col(5).addChild(
                                new Row().addChild(
                                        WBshooter).addChild(
                                                WBintake))
                                .addChild(
                                        new Row().addChild(
                                                WBtransfer).addChild(
                                                        WBindexer))
                                                )
                .setTitle("Subsystems"));

        // Autos — path visualiser + path/auto cross-reference map.
        // The panel writes its own data to /dynamic/autoanalysis.json on start;
        // only the armed auto name comes back over the socket.
        // onArm runs on the robot thread (AutoTools defers it to update()), so
        // building the command here is safe.
        AutoTools tools = new AutoTools();
        tools.onArm((String name) -> {
            autoChosen.accept(new PathPlannerAuto(name));
            WBautoChooser.setArmed(name, tools.getWarnCount(name) > 0);
        });
        WBautoTools = tools;

        dashboard.addTab(new Tab()
                .setTitle("Autos")
                .addChild(new Col(12).addChild(WBautoTools)));

        dashboard.addPanel(new LooptimeMonitor());
        dashboard.addPanel(new PingMonitor());
        dashboard.addPanel(new FPSMonitor());
        dashboard.addPanel(WBalarms);
        dashboard.start();
    }

    public void update() {
        try {
            updateTelemetry();
        } catch (Exception e) {
            // A telemetry read that throws must not stop dashboard.update()
            // below — that call is what flushes every panel's messages, so
            // losing it silently breaks the whole board.
            if (!telemetryFaulted) {
                telemetryFaulted = true;
                System.err.println("[Dashboard] telemetry update failed, "
                        + "panels will keep running: " + e);
                e.printStackTrace();
            }
        }
        dashboard.update();
    }

    private boolean telemetryFaulted = false;

    private void updateTelemetry() {
        Pose2d pose = drivetrain.getPose();
        WBfieldMap.sendPose((DriverStation.getAlliance().isPresent() && DriverStation.getAlliance().get() == Alliance.Red) ? 16.0-pose.getY():pose.getY(), pose.getX(), pose.getRotation().getDegrees());

        //TODO implement displays
        /*WBshooter.updateVals(shooter.getRPS(), (shooter.getLeadTemp()+shooter.getFollowTemp())/2.0);
        WBintake.updateVals(intake.getVel(), intake.getTemp());
        WBindexer.updateVals(indexer.getRPS1(), indexer.getTemp1());
        WBindexer.updateVals(indexer.getRPS2(), indexer.getTemp2());
        WBtransfer.updateVals(transfer.getRPS(), transfer.getTemp());*/

        double[] swerveAngles = new double[4];
        double[] swerveTemps = new double[4];
        double[] swerveVels = new double[4];
        SwerveModuleState[] states = drivetrain.getState().ModuleStates;
        SwerveModule<TalonFX, TalonFX, CANcoder>[] modules = drivetrain.getModules();

        for (int i = 0; i < states.length; i++) {
            swerveAngles[i] = states[i].angle.getDegrees();
            swerveTemps[i] = drivetrain.getModules()[i].getDriveMotor().getDeviceTemp().getValueAsDouble();
            swerveVels[i] = states[i].speedMetersPerSecond;
        }

        WBswerveModules.updateVals(swerveAngles, swerveTemps, swerveVels);

        // Master Alarms update

        for (SwerveModule<TalonFX, TalonFX, CANcoder> module : modules) {
            if (module.getDriveMotor().getDeviceTemp().getValueAsDouble() > 80) {
                WBalarms.triggerAlarm(0);
            }
        }

        double maxHeat = 70.0;
        //TODO implement temp alarm
        /*  if (
            shooter.getHoodTemp() > maxHeat || shooter.getLeadTemp() > maxHeat ||
            shooter.getFollowTemp() > maxHeat || turret.getTemp() > maxHeat ||
            intake.getTemp() > maxHeat || intakeExtension.getTemp() > maxHeat ||
            indexer.getTemp1() > maxHeat || indexer.getTemp2() > maxHeat || transfer.getTemp() > maxHeat

        ) {
            WBalarms.triggerAlarm(1);
        }*/

        CANStatus can = RobotController.getCANStatus();
        if (can.transmitErrorCount > 0 || can.receiveErrorCount > 0 || can.percentBusUtilization > 0.9) {
            WBalarms.triggerAlarm(2);
        }

        /*currAvg = (currAvg + (pdh.getTotalCurrent() * 0.1)) / 1.1;
        if (currAvg > 150.0) {
            WBalarms.triggerAlarm(3);
        }*/

        if (!DriverStation.getJoystickIsXbox(0)) {
            WBalarms.triggerAlarm(4);
        }

        // 5, ping, is handled by frontend

        if (WildBoard.loopTime_ms > 100) {
            WBalarms.triggerAlarm(6);
        }

        battAvg = (battAvg + (RobotController.getBatteryVoltage() * 0.1)) / 1.1;
        if (battAvg < 10.0) {
            WBalarms.triggerAlarm(7);
        }
    }
}