package frc.robot.WildBoard.Panels;

import frc.robot.WildBoard.WBPanel;

public class MasterAlarms extends WBPanel {
    private int repeats = 0;
    private int repeatInterval = 45 + (int) Math.round(Math.random() * 10); // add some randomization to prevent offset panels
    private boolean alarmsChanged = false;
    private boolean[] alarms;

    public MasterAlarms(String[] labels, int cols) {
        this.usesML = true;

        this.alarms = new boolean[labels.length];
        for (int i = 0; i < this.alarms.length; i++) {
            this.alarms[i] = false;
        }

        this.setPanelName("MasterAlarms");
        this.addRawProp("texts", "[\"" + String.join("\",\"", labels) + "\"]");
        this.addProp("cols", cols);
    }

    public MasterAlarms(String[] labels, String[] desc, int cols) {
        this.usesML = true;

        this.alarms = new boolean[labels.length];
        for (int i = 0; i < this.alarms.length; i++) {
            this.alarms[i] = false;
        }

        this.setPanelName("MasterAlarms");
        this.addRawProp("texts", "[\"" + String.join("\",\"", labels) + "\"]");
        this.addRawProp("descriptions", "[\"" + String.join("\",\"", desc) + "\"]");
        this.addProp("cols", cols);
    }

    public void triggerAlarm(int index) {
        this.alarms[index] = true;
        this.alarmsChanged = true;
    }

    public void clearAlarm(int index) {
        this.alarms[index] = false;
        this.alarmsChanged = true;
    }

    public void setAlarm(int index, boolean value) {
        this.alarms[index] = value;
        this.alarmsChanged = true;
    }

    public boolean[] getAlarms() {
        return this.alarms;
    }

    private static String boolArrayToBinaryString(boolean[] arr) {
        StringBuilder sb = new StringBuilder(arr.length);

        for (boolean b : arr) {
            sb.append(b ? '1' : '0');
        }

        return sb.toString();
    }

    @Override
    public void onMsg(String msg) {
        if (msg.startsWith("c")) {
            for (int i = 0; i < this.alarms.length; i++) {
                this.alarms[i] = false;
            }
            this.alarmsChanged = true;
        }
        if (msg.startsWith("t")) {
            int index = Integer.parseInt(msg.substring(1));
            if (this.alarms[index]) {
                return;
            }
            this.alarms[index] = true;
            this.alarmsChanged = true;
        }
    }

    @Override
    public void update() {
        if (!alarmsChanged && repeats < repeatInterval) {
            repeats++;
            return;
        }
        this.ml.send(boolArrayToBinaryString(this.alarms));
        alarmsChanged = false;
        repeats = 0;
    }
}
