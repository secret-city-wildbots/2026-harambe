package frc.robot.WildBoard.Panels;

import frc.robot.WildBoard.WBPanel;

public class CameraFeed extends WBPanel {
    public CameraFeed(int PORT) {
        this.setPanelName("CameraFeed");
        this.addProp("port", PORT);
    }
}
