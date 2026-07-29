package frc.robot.WildBoard.Panels;

import frc.robot.WildBoard.WBPanel;
import frc.robot.WildBoard.WildBoard;

public class LooptimeMonitor extends WBPanel {

    public LooptimeMonitor() {
        this.usesML = true;

        this.setPanelName("LooptimeMonitor");
    }

    @Override
    public void update() {
        this.ml.send(WildBoard.loopTime_ms + "");
    }
}