package frc.robot.WildBoard.Panels;

import java.util.Arrays;
import java.util.function.BiConsumer;

import frc.robot.WildBoard.WBPanel;

public class Overrides extends WBPanel {
    public BiConsumer<Integer, Boolean> handleChange;
    public boolean[] overrideVals;
    public boolean compMode = false;

    public Overrides(String[] switches, int cols) {
        this.usesML = true;

        this.setPanelName("Overrides");

        this.overrideVals = new boolean[switches.length];
        for (int i = 0; i < overrideVals.length; i++) {
            this.overrideVals[i] = false;
        }

        String blah = "";
        for (String s : switches) {
            blah+="{ label: \""+s+"\"},";
        }
        this.addRawProp("switches", "[" + blah.substring(0, blah.length() - 1) + "]");
        this.addProp("columns", cols);
    }

    /**
     * Takes in a function that will be called when a switch is toggled. The function should take in the index of the switch and the new value of the switch.
     * @param changeHandler
     */
    public Overrides onChange(BiConsumer<Integer, Boolean> changeHandler) {
        this.handleChange = changeHandler;
        return this;
    }

    @Override
    public void onMsg(String msg) {
        if (msg.startsWith("C")) {
            this.compMode = msg.substring(1).startsWith("1");
            return;
        }

        Boolean[] newVals = Arrays.stream(msg.split(",")).map(s -> (s == "1") ? true : false).toArray(Boolean[]::new);
        for (int i = 0; i < newVals.length; i++) {
            if (newVals[i] != overrideVals[i]) {
                overrideVals[i] = newVals[i];
                handleChange.accept(i, newVals[i]);
            }
        }
    }
}
