package com.scapelike.server;

import com.scapelike.db.Database;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);

    private final Database db;
    private final Map<String, WsContext> sessions = new ConcurrentHashMap<>();

    public GameServer(Database db) {
        this.db = db;
    }

    public void start() {
        Javalin app = Javalin.create(
                config -> config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost())));

        app.get("/health", ctx -> ctx.result("OK"));

        app.ws("/ws/game", ws -> {
            ws.onConnect(ctx -> {
                sessions.put(ctx.sessionId(), ctx);
                log.info("Client connected: {}", ctx.sessionId());
            });
            ws.onMessage(ctx -> {
                log.debug("Message [{}]: {}", ctx.sessionId(), ctx.message());
                // TODO: route messages to game logic
            });
            ws.onClose(ctx -> {
                sessions.remove(ctx.sessionId());
                log.info("Client disconnected: {}", ctx.sessionId());
            });
        });

        app.start(7070);
        log.info("Game server listening on :7070");
    }
}
