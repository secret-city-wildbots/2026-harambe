// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
    public static class RobotConstants { 
      public static final double driveCurLim = 35.0;
    }
    public static class IntakeConstants {
    // Intake motor CANBus IDs
    public static final int intakeMotorID = 54;
    public static final int extensionMotorID = 41;

    // Gear Ratios
    public static final double extensionGearRatio = 81.0;

    // Intake Ranges
    public static final double minDegree = 0.0;
    public static final double maxDegree = 86.0;
  }

  public static class ElevatorConstants {
    // Elevator motor CANBus IDs
    public static final int liftMotorID = 48;
    public static final int hookMotorID = 49;
    public static final int CANifierID = 51; 
    public static final double maxSpeedPercentage = 0.08;

    // Sensor ID / Ports
    public static final int lowerLimitMagneticSensorPort = 0;
    public static final int handoffMagneticSensorPort = 1;
    public static final int topLimitMagneticSensorPort = 2;

    // Hook Cancoder CANBus IDs
    public static final int hookMotorCancoderID = 34;
    public static final double hookEncoderOffset = 0.0794921875;

    // Hook motor positions
    public static final double hookSafePosition = 0.0;
    public static final double hookGuideDeployedPosition = 0.1;
    public static final double hookDeployedPosition = 0.18;
    public static final double hookPosForTopRungClearance = 43.56;
    public static final double angleTolerance = 2.0; 
  }

  public static class ShooterConstants {
    // Intake motor CANBus IDs
    public static final int leadMotorID = 42;
    public static final int followerMotorID = 43;
  }

  public static class TransferConstants {
    public static final int motorID = 44;
  }

  public static class IndexerConstants {
    // Intake motor CANBus IDs
    public static final int indexerMotorID = 40;
  }

  public static class VisionConstants {
    // Limelight Names
    public static final String[] limelightNames = {"limelight-front", "limelight-back", "limelight-left", "limelight-right"};
  }
}