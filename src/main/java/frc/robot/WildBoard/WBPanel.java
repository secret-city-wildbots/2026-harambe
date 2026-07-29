package frc.robot.WildBoard;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

public class WBPanel {
    private String panelName = "Placeholder";
    public ArrayList<WBPanel> children = new ArrayList<>();
    private Dictionary<String, String> props = new Hashtable<>();
    private Dictionary<String, String> rawProps = new Hashtable<>();
    private Dictionary<String, Integer> intprops = new Hashtable<>();
    private ArrayList<String> boolprops = new ArrayList<>();
    public boolean usesML = false;
    public int id;
    public MessageLayer ml;

    public void setProps(Dictionary<String, String> propsDic) {
        this.props = propsDic;
    }

    public void addRawProp(String key, String val) {
        this.rawProps.put(key, val);
    }

    public void addProp(String key, String val) {
        this.props.put(key, val);
    }

    public void addProp(String key, int val) {
        this.intprops.put(key, val);
    }

    public void addProp(String key, boolean val) {
        if (val)
            this.boolprops.add(key);
    }

    public void appendToProp(String key, String val) {
        if (this.props.get(key) == null) {
            this.props.put(key, val);
        } else {
            this.props.put(key, this.props.get(key) + val);
        }
    }

    public String getProp(String key) {
        return this.props.get(key);
    }

    public void setPanelName(String name) {
        this.panelName = name;
    }

    public String getPanelName() {
        return this.panelName;
    }

    public void start() {
    }

    public void update() {
    }

    public void assignML(MessageLayer ml, int id) {
        if (this.ml == null) {
            ml.bind(this::onMsg);
        }
        this.ml = ml;
        this.id = id;
    }

    /**
     * Does nothing by default, override to run it when a message is received from the dashboard
     * @param msg
     */
    public void onMsg(String msg) {
    }

    public String generate() {

        String propsString = "";

        if (this.usesML) {
            this.addProp("id", id);
            propsString = "socket={socket} ";
        }

        Enumeration<String> e = props.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            propsString += key + "={\"" + props.get(key) + "\"} ";
        }

        e = intprops.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            propsString += key + "={" + intprops.get(key) + "} ";
        }

        e = rawProps.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            propsString += key + "={" + rawProps.get(key) + "} ";
        }

        for (String key : boolprops) {
            propsString += key + " ";
        }

        String childrenString = "";
        for (WBPanel panel : children) {
            childrenString += panel.generate();
        }

        return String.format("<%s %s>%s</%s>",
                panelName, propsString, childrenString, panelName);
    }

    public String genImport() {
        String childImports = "";
        for (WBPanel child : children) {
            childImports += child.genImport() + "\n";
        }
        return childImports +
                "import " + panelName +
                " from \"[DEPLOY]/WildBoard/frontend/src/panels/" +
                panelName + ".tsx\";";
    }
}