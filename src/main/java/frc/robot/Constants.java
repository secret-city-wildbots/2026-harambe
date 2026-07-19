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
    public static class IntakeConstants {
    // Intake motor CANBus IDs
    public static final int intakeMotorID = 40;
    public static final int extensionMotorID = 41;

    // Gear Ratios
    public static final double extensionGearRatio = 81.0;

    // Intake Ranges
    public static final double minDegree = 0.0;
    public static final double maxDegree = 86.0;
  }

  public static class ShooterConstants {
    // Intake motor CANBus IDs
    public static final int leadMotorID = 42;
    public static final int followerMotorID = 43;
  }

  public static class TransferConstants {
    public static final int motorID = 44;
  }
}