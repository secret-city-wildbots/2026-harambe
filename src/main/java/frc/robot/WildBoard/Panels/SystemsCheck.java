package frc.robot.WildBoard.Panels;

import frc.robot.WildBoard.WBPanel;

public class SystemsCheck extends WBPanel {
    private Runnable onTest;

    public SystemsCheck() {
        this.usesML = true;

        this.setPanelName("SystemsCheck");
    }

    /**
     * triggers when test button is pressed
     * @param handler
     */
    public SystemsCheck onTest(Runnable handler) {
        this.onTest = handler;
        return this;
    }

    @Override
    public void onMsg(String msg) {
        if (msg.startsWith("C1")) {
            this.onTest.run();
        }
    }
}
