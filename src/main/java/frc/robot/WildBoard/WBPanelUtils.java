package frc.robot.WildBoard;

import java.util.function.Consumer;

public final class WBPanelUtils {

    public static void traverse(WBPanel panel, Consumer<WBPanel> action) {
        action.accept(panel);

        for (WBPanel child : panel.children) { // or panel.getChildren()
            traverse(child, action);
        }
    }
}