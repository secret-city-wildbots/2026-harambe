package frc.robot.WildBoard.Panels;

import frc.robot.WildBoard.WBPanel;

public class NumberDisplay extends WBPanel {
    public NumberDisplay(String label) {
        this.usesML = true;
        this.setPanelName("NumberDisplay");
        this.addProp("label", label);
    }

    public NumberDisplay(String label, int chars) {
        this.usesML = true;
        this.setPanelName("NumberDisplay");
        this.addProp("label", label);
        this.addProp("chars", chars);
    }

    public void updateDisplay(String text) {
        this.ml.send(text);
    }
}
