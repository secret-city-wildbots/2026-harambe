package frc.robot.WildBoard;

import java.util.function.Consumer;

public class MessageLayer {
    public Server server;
    public int elemID;
    private String queue = "";

    public MessageLayer(Server server, int elemID) {
        this.server = server;
        this.elemID = elemID;
    }

    public void send(String msg) {
        // enqueue instead of immediate websocket send
        this.queue+=("e" + elemID + "." + msg);
    }

    public void update() {
        this.server.ws.enqueue(queue);
        queue = "";
    }

    /**
     * Bind to incoming messages on this id. ONLY DO ONCE PER PANEL
     * @param handler
     */
    public void bind(Consumer<String> handler) {
        this.server.ws.bind(elemID, handler);
    }
}
