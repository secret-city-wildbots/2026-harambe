export type WsDataHandler = (data: string) => void;
export type WsPingHandler = (latency: number) => void;

export class WsEventBus {
  private ws: WebSocket;

  private handlers: Map<number, Set<WsDataHandler>> = new Map();
  private pingHandlers: WsPingHandler[] = [];
  private fpsHandlers: WsPingHandler[] = [];

  private connected = false;

  // incoming websocket chunks waiting to be processed
  private incomingQueue: string[] = [];

  // parser buffer
  private recvBuffer = "";

  // ping
  private lastPing = performance.now();
  private lastFrame = performance.now();
  private rollingFPS = [60];
  public latency = 100;
  public fps = 60;

  // processing limits
  private readonly PROCESS_BUDGET_MS = 5; // max parsing time per frame

  constructor(url: string) {
    console.log("WS BUS CREATED");

    this.ws = new WebSocket(url);

    this.ws.onopen = () => {
      this.connected = true;
      console.log("[WS] Connected");
    };

    this.ws.onclose = () => {
      this.connected = false;
      console.warn("[WS] Disconnected");
    };

    this.ws.onerror = (e) => {
      console.error("[WS] Error:", e);
    };

    this.ws.onmessage = (event) => {
      if (typeof event.data !== "string") return;

      // ping packet
      if (event.data === "p") {
        this.latency = performance.now() - this.lastPing;

        this.pingHandlers.forEach((h) => h(this.latency));
        return;
      }

      //console.info(event.data);
      // DO NOT parse immediately — queue it
      this.incomingQueue.push(event.data);
    };

    // start frame-based processing loop
    requestAnimationFrame(this.processLoop);

    // stable ping interval
    setInterval(() => {
      if (!this.connected) return;
      this.lastPing = performance.now();
      this.sendRaw("p");
    }, 500);
  }

  // =============================
  // FRAME PROCESSING LOOP
  // =============================
  private processLoop = () => {
    const start = performance.now();

    let processedFrame = false;
    if (this.incomingQueue.length > 0) {
      processedFrame = true;
    }

    // process messages until we hit budget
    while (
      this.incomingQueue.length > 0 &&
      performance.now() - start < this.PROCESS_BUDGET_MS
    ) {
      this.recvBuffer += this.incomingQueue.shift()!;
      this.parseBufferedMessages();
    }

    if (processedFrame && performance.now() - this.lastFrame > 0) {
      if (this.rollingFPS.length >= 100) {
        this.rollingFPS.shift();
      }
      this.rollingFPS.push(1000 / (performance.now() - this.lastFrame));

      this.fps = this.rollingFPS.reduce((a, b) => a + b, 0) / this.rollingFPS.length;
      this.fpsHandlers.forEach((h) => h(this.fps));
    }
    this.lastFrame = performance.now();

    requestAnimationFrame(this.processLoop);
  };

  // =============================
  // PARSER
  // =============================
  private parseBufferedMessages() {
    let i = 0;

    while (true) {
      let colon = i;

      // parse length prefix
      while (colon < this.recvBuffer.length) {
        const c = this.recvBuffer.charCodeAt(colon);

        if (c === 58) break; // ':'
        if (c < 48 || c > 57) {
          console.warn("[WS] Invalid frame");
          this.recvBuffer = "";
          return;
        }

        colon++;
      }

      // incomplete length
      if (colon >= this.recvBuffer.length) break;

      const len = Number(this.recvBuffer.slice(i, colon));
      if (!Number.isFinite(len) || len < 0) {
        console.warn("[WS] Bad length");
        this.recvBuffer = "";
        return;
      }

      const start = colon + 1;
      const end = start + len;

      // incomplete message
      if (end > this.recvBuffer.length) break;

      const msg = this.recvBuffer.slice(start, end);

      this.handleMessage(msg);

      i = end;
    }

    // remove parsed bytes
    if (i > 0) {
      this.recvBuffer = this.recvBuffer.slice(i);
    }
  }

  // =============================
  // MESSAGE DISPATCH
  // =============================
  private handleMessage(msg: string) {
    // expected: e[id].[data]
    if (!msg.startsWith("e")) return;

    const dotIndex = msg.indexOf(".");
    if (dotIndex === -1) return;

    const idStr = msg.substring(1, dotIndex);
    const data = msg.substring(dotIndex + 1);

    const id = Number(idStr);
    if (Number.isNaN(id)) return;

    const listeners = this.handlers.get(id);
    if (!listeners) return;

    // handlers still run sync — but parsing is now frame-budgeted
    for (const handler of listeners) {
      handler(data);
    }
  }

  // =============================
  // SUBSCRIPTIONS
  // =============================
  subscribe(id: number, handler: WsDataHandler): () => void {
    let set = this.handlers.get(id);

    if (!set) {
      set = new Set();
      this.handlers.set(id, set);
    }

    set.add(handler);

    return () => {
      set!.delete(handler);
      if (set!.size === 0) {
        this.handlers.delete(id);
      }
    };
  }

  subscribePing(handler: WsPingHandler): () => void {
    this.pingHandlers.push(handler);

    return () => {
      this.pingHandlers = this.pingHandlers.filter(
        (h) => h !== handler
      );
    };
  }

  subscribeFPS(handler: WsPingHandler): () => void {
    this.fpsHandlers.push(handler);

    return () => {
      this.fpsHandlers = this.fpsHandlers.filter(
        (h) => h !== handler
      );
    };
  }

  // =============================
  // SEND
  // =============================
  send(id: number, msg: string) {
    this.sendRaw("r" + id + "." + msg);
  }

  sendRaw(text: string) {
    if (this.connected) {
      this.ws.send(text);
    }
  }

  close() {
    this.ws.close();
  }
}