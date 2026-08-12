package frc.robot.WildBoard.Panels;

import frc.robot.WildBoard.WBPanel;

/**
 * TeleOp readout showing which auto is currently armed.
 *
 * <p>This used to be a free-text input. Autos are now picked in the "Autos"
 * tab, which validates that the auto will actually load before arming it, so
 * this panel is display-only — it exists so the driver can confirm at a glance
 * what will run.
 *
 * <p>Messages to the frontend: {@code <name>|<state>} where state is
 * {@code ok}, {@code warn} or {@code none}. Messages from the frontend:
 * {@code hello}, sent on page load to ask for the current value.
 */
public class AutoChooser extends WBPanel {

    private String armed = "";
    private String state = "none";
    private String lastSent = null;
    private volatile boolean helloRequest = false;

    public AutoChooser() {
        this.usesML = true;
        this.setPanelName("AutoChooser");
    }

    /**
     * Set the armed auto.
     *
     * @param name  the auto name, or "" for none
     * @param warns true if the auto loads but the analysis flagged warnings
     */
    public void setArmed(String name, boolean warns) {
        this.armed = name == null ? "" : name;
        this.state = this.armed.isEmpty() ? "none" : (warns ? "warn" : "ok");
    }

    public void setArmed(String name) {
        setArmed(name, false);
    }

    public String getArmed() {
        return this.armed;
    }

    @Override
    public void onMsg(String msg) {
        if ("hello".equals(msg)) this.helloRequest = true;
    }

    @Override
    public void update() {
        if (this.ml == null) return;
        String payload = this.armed + "|" + this.state;
        if (this.helloRequest || !payload.equals(this.lastSent)) {
            this.helloRequest = false;
            this.lastSent = payload;
            this.ml.send(payload);
        }
    }
}
