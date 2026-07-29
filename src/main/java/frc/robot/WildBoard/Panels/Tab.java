package frc.robot.WildBoard.Panels;

import frc.robot.WildBoard.WBPanel;

public class Tab extends WBPanel {
    private String title = "Placeholder";

    /**
     * Set the Tab label
     */
    public Tab setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return this.title;
    }

    /**
     * Add a child
     */
    public Tab addChild(WBPanel child) {
        this.children.add(child);
        return this;
    }

    @Override
    public String genImport() {
        String childImports = "";
        for (WBPanel child: this.children) {
            childImports+=child.genImport()+"\n";
        }
        return childImports;
    }

    @Override
    public String generate() {
        String childString = "";
        for (WBPanel child: this.children) {
            childString+=child.generate();
        }
        return "{title: \""+this.title+"\",content: (<Container><div class=\"row\">"+childString+"</div></Container>)},";
    }
}
