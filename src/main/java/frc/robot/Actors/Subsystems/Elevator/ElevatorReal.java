
package frc.robot.Actors.Subsystems.Elevator;

// Import WPILib Libraries
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

// Import Phoenix 6 Libraries
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix.CANifier;
import com.ctre.phoenix6.configs.MagnetSensorConfigs;

// Import Actors, Utils & Constants
import frc.robot.Actors.Motor;
import frc.robot.Utils.MotorType;
import frc.robot.Utils.RotationDir;
import frc.robot.Constants.ElevatorConstants;

public class ElevatorReal implements Elevator {

    // Define variables
    public Motor motorLift; // Motor to control the elevator hook position
    private CANcoder encoder; // Encoder to control the position of the hook
    private double targetAngle; // Target angle to set the hook position to

    // Define variables
    public Motor motorHooks;
   
    // private DigitalInput handoffLimitMagneticSwitch; // Handoff limit magnetic
    // switch for the elevator lift
    private CANifier handoffLimitSwitch; // Handoff limit magnetic switch for the elevator lift

    // code to rotate 30 motor rotations
    private double initMotorRotations = -999999.0; // Picked a vaule that cannot be reached to indicate it is not set
    private double motorRotationsSinceTopLimitSwitch = 0.0;

    public ElevatorReal() {
        // Configure the elevator hook motor
        this.motorLift = new Motor(ElevatorConstants.hookMotorID, MotorType.TFX, "rio");
        this.motorLift.motorConfig.direction = RotationDir.Clockwise;
        this.motorLift.applyConfig();

        // Configure the elevator hook encoder
        this.encoder = new CANcoder(ElevatorConstants.hookMotorCancoderID);
        MagnetSensorConfigs config = new MagnetSensorConfigs();
        config.withMagnetOffset(ElevatorConstants.hookEncoderOffset);
        this.encoder.getConfigurator().apply(config);

        // Initialize the target angle to be 0.0 degrees
        this.targetAngle = 0.0;

        // Configure the elevator lift motor
        this.motorHooks = new Motor(ElevatorConstants.liftMotorID, MotorType.TFX, "rio");
        this.motorHooks.motorConfig.direction = RotationDir.CounterClockwise;
        this.motorHooks.applyConfig();

        this.motorHooks.powersave();

        // Configure the elevator magnetic switches
        //this.lowerLimitMagneticSwitch = new CANifier(find the ID);
        // this.handoffLimitMagneticSwitch = new DigitalInput(ElevatorConstants.handoffMagneticSensorPort);
        this.handoffLimitSwitch = new CANifier(ElevatorConstants.CANifierID);
    }

    public double getTemp() {
        return this.motorLift.getTemp();
    }

    // Motor Controls

    /**
     * Sets ElevatorHook motor output (-1.0 to 1.0)
     * 
     * @param percent
     */
    public void setHooks(double percent) {
        // Clamp input percentage to proper range
        percent = MathUtil.clamp(percent, -1.0, 1.0);

        // Check to make sure the hooks are safe to extend out
        if (percent > 0.0 && getCurrentAngle() >= ElevatorConstants.hookDeployedPosition) {
            // if it is not safe, dont allow the motor to move
            motorHooks.dc(0.0);
            return;
        }

        // check to make sure the hooks are safe to retract in
        if (percent < 0.0 && getCurrentAngle() <= ElevatorConstants.hookSafePosition) {
            // if it is not safe, dont allow the motor to move
            motorHooks.dc(0.0);
            return;
        }

        // Send the output to the motor
        motorHooks.dc(percent);
    }

    public void setLift(double percent) {
        // Clamp input percentage to proper range
        percent = MathUtil.clamp(percent, -1.0, 1.0);

        // Set initMotorRotations if the top limit is active and it has not been set
        if (topLimitActive() && this.initMotorRotations == -999999.0) {
            // Set motor rotations
            this.initMotorRotations = motorLift.pos();
        }


        if (!topLimitActive()) {
            // reset variables
            this.initMotorRotations = -999999.0;
            this.motorRotationsSinceTopLimitSwitch = 0.0;
        } else {
            // keep track of motor rotations
            this.motorRotationsSinceTopLimitSwitch = motorLift.pos();
        }

        // Check to make sure the elevator is safe to move up
        if (percent < 0.0 && topLimitActive() && Math.abs(Math.abs(this.motorRotationsSinceTopLimitSwitch) - Math.abs(this.initMotorRotations)) > 12.5) {
            // if it is not safe, dont allow the motor to move
            motorLift.dc(0.0);
            return;
        }

        // check to make sure the elevator is safe to move down
        // if (percent > 0.0 && lowerLimitActive() && handoffLimitActive()) {
        //     // if it is not safe, dont allow the motor to move
        //     motor.dc(0.0);
        //     return;
        // }

        // Send the output to the motor
        motorLift.dc(percent);
    }

    /**
     * Get lower limit switch made status.
     * 
     * @return active status (sensor is made)
     */

     public boolean lowerLimitActive() {
        return  !this.handoffLimitSwitch.getGeneralInput(CANifier.GeneralPin.QUAD_A); // beam break
     }

     /**
     * Get handoff limit switch made status.
     * 
     * @return active status (sensor is made)
     */

     public boolean handoffLimitActive() {
        // return  this.handoffLimitMagneticSwitch.get();
        return !this.handoffLimitSwitch.getGeneralInput(CANifier.GeneralPin.LIMF); // bottom magnet
     }

     /**
     * Get top limit switch made status.
     * 
     * @return active status (sensor is made)
     */

     public boolean topLimitActive() {
        return  !this.handoffLimitSwitch.getGeneralInput(CANifier.GeneralPin.LIMR); //top magnet
     }

    /**
     * Sets the target angle for the hooks to travel to
     * 
     * @param angle
     */
    public void setTargetAngle(double angle) {
        this.targetAngle = angle;
    }

    /**
     * Returns if we can continue to climb after the top limit switch is active
     * 
     * @return true to continue to lift false to false to stop lifting
     */
     public boolean climbAfterTopLimitSwitch() {
        if (topLimitActive()) {
            return Math.abs(Math.abs(this.motorRotationsSinceTopLimitSwitch) - Math.abs(this.initMotorRotations)) > 12.5;
        } 
        return false;
     }

    // Sensor Controls

    /**
     * Get the encoder position in degrees
     */
    public double getCurrentAngle() {
        // We get the absolute position of the encoder (-1 - 1 rotations) and multiply
        // by 360 to get degrees
        return this.encoder.getAbsolutePosition().getValueAsDouble();
    }

    /**
     * Get the target position in degrees
     */
    public double getTargetAngle() {
        // We return the target angle
        return this.targetAngle;
    }

    private final CANifier.PinValues pins = new CANifier.PinValues();

    @Override
    public void periodic() {
        // this.handoffLimitSwitch.getGeneralInputs(pins);
        // System.out.println(
        //         "CANifier fw=" + this.handoffLimitSwitch.getFirmwareVersion()
        //                 + " bus=" + this.handoffLimitSwitch.getBusVoltage()
        //                 + " err=" + this.handoffLimitSwitch.getLastError()
        //                 + " | QUAD_A=" + pins.QUAD_A
        //                 + " QUAD_B=" + pins.QUAD_B
        //                 + " QUAD_IDX=" + pins.QUAD_IDX
        //                 + " LIMF=" + pins.LIMF
        //                 + " LIMR=" + pins.LIMR
        //                 + " SDA=" + pins.SDA
        //                 + " SCL=" + pins.SCL
        //                 + " SPI_CS=" + pins.SPI_CS_PWM3
        //                 + " SPI_MISO=" + pins.SPI_MISO_PWM2
        //                 + " SPI_MOSI=" + pins.SPI_MOSI_PWM1
        //                 + " SPI_CLK=" + pins.SPI_CLK_PWM0);
         System.out.println("Low Lim: "+lowerLimitActive()+" Mid Lim: "+handoffLimitActive()+" Upper Lim: "+topLimitActive());
         System.out.println("Current Angle: "+getCurrentAngle()+" Target Angle: "+getTargetAngle());
         System.out.println("Init Motor Rotations: "+this.initMotorRotations+" Motor Rotations Since Top Limit Switch: "+this.motorRotationsSinceTopLimitSwitch);
    }
}