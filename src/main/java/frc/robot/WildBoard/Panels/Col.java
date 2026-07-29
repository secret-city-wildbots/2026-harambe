package frc.robot.WildBoard.Panels;

import frc.robot.WildBoard.WBPanel;

public class Col extends WBPanel {
    private int width;
    /**
     * A column for use in a row, where all columns add up to a width of 12
     * @param width the width of the column from 1-12
     */
    public Col(int width) {
        this.width = width;
        this.setPanelName("Col");
    }

    /**
     * Add a child
     */
    public Col addChild(WBPanel child) {
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
                childrenString+= "<div class=\"column-item bubble\">" + panel.generate() + "</div>";
            }
        }
        return "<div class=\"col-"+this.width+" column\">"+childrenString+"</div>";
    }

    @Override
    public String genImport() {
        String childImports = "";
        for (WBPanel child: this.children) {
            childImports+=child.genImport()+"\n";
        }
        return childImports;
    }
}
