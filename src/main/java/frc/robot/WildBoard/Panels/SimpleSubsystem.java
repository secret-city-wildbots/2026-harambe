package frc.robot.WildBoard.Panels;

import java.util.function.Consumer;

import frc.robot.WildBoard.WBPanel;

public class SimpleSubsystem extends WBPanel {
    private Consumer<Boolean> unlockHandler;
    private Consumer<Boolean> onCalibHandler;

    public SimpleSubsystem(String name, boolean absolute) {
        this.usesML = true;

        this.setPanelName("SimpleSubsystem");
        this.addProp("name", name);
        this.addProp("absolute", absolute);
        this.addProp("velocity", false);
    }

    public SimpleSubsystem(String name, boolean absolute, String unit) {
        this.usesML = true;

        this.setPanelName("SimpleSubsystem");
        this.addProp("name", name);
        this.addProp("absolute", absolute);
        this.addProp("unit", unit);
        this.addProp("velocity", false);
    }

    public void updateVals(double pos, double temp_C) {
        this.ml.send(pos + "," + temp_C);
    }

    /**
     * triggers when unlocked or locked, with the new state as a parameter
     * @param handler (true for locked, false for unlocked)
     */
    public void onUnlock(Consumer<Boolean> handler) {
        this.unlockHandler = handler;
    }

    /**
     * triggers when calibration button is pressed, with the new state as a parameter
     * @param handler (true for pressed, false for released)
     */
    public void onCalib(Consumer<Boolean> handler) {
        this.onCalibHandler = handler;
    }

    @Override
    public void onMsg(String msg) {
        if (msg.startsWith("c")) {
            this.onCalibHandler.accept(msg.charAt(1) == '1');
        } else {
            this.unlockHandler.accept(msg.charAt(0) == 'l');
        }
    }
}