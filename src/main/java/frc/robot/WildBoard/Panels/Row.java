package frc.robot.WildBoard.Panels;

import frc.robot.WildBoard.WBPanel;

public class Row extends WBPanel {
    public Row() {
        this.setPanelName("DashboardSubRow");
    }

    /**
     * Add a child
     */
    public Row addChild(WBPanel child) {
        this.children.add(child);
        return this;
    }

    @Override
    public String generate() {
        String childrenString = "";
        for (WBPanel panel: this.children) {
            if (panel.getPanelName() == "DashboardSubRow") {
                childrenString+=panel.generate();
            } else {
                childrenString+= "<div class=\"bubble\">" + panel.generate() + "</div>";
            }
        }
        return "<DashboardSubRow>"+childrenString+"</DashboardSubRow>";
    }
}
