package frc.robot.WildBoard.Panels;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.robot.WildBoard.WBPanel;

public class FieldMap extends WBPanel {
    private double blah = 0;

    public FieldMap() {
        this.usesML = true;
        
        this.setPanelName("FieldMap");
        if (DriverStation.getAlliance().isPresent()) {
            this.addProp("alliance", (DriverStation.getAlliance().get() == Alliance.Red));
        } else {
            this.addProp("alliance", true);
            System.out.println("Alliance information not found, defaulting to Red. This should only happen in simulation.");
        }
    }

    public void sendPose(double x, double y, double h) {
        this.ml.send(Math.round(x*10)/10.0 + "," + Math.round(y*10)/10.0 + "," + Math.round(h*10)/10.0);
    }
}