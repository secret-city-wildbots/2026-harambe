package frc.robot.WildBoard.Panels;

import java.util.function.Consumer;

import frc.robot.WildBoard.WBPanel;

public class AutoChooser extends WBPanel {
    private Consumer<String> onChange;

    public AutoChooser(String[] autos) {
        this.usesML = true;

        this.setPanelName("AutoChooser");
        this.addRawProp("autos", "[\"" + String.join("\",\"", autos) + "\"]");
    }

    /**
     * triggers when test button is pressed
     * @param handler
     */
    public AutoChooser onChange(Consumer<String> handler) {
        this.onChange = handler;
        return this;
    }

    @Override
    public void onMsg(String msg) {
        this.onChange.accept(msg);
    }
}
