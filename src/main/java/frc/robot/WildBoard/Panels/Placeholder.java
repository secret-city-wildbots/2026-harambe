package frc.robot.WildBoard.Panels;

import frc.robot.WildBoard.WBPanel;

public class Placeholder extends WBPanel {
    public Placeholder(String text) {
        this.setPanelName("Placeholder");
        this.addProp("text", text);
    }
    public Placeholder(String text, int height) {
        this.setPanelName("Placeholder");
        this.addProp("text", text);
        this.addProp("height", height);
    }
}
