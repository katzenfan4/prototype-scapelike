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

    // TODO: inject into message handlers
    private final Database db;
    private final Map<String, WsContext> sessions = new ConcurrentHashMap<>();

    public GameServer(Database db) {
        this.db = db;
    }

    public void start() {
        int port = Integer.parseInt(env("PORT", "7070"));
        Javalin app = Javalin.create(
                config -> config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost())));

        app.get("/health", ctx -> ctx.result("OK"));

        app.ws("/ws/game", wsConfig -> {
            wsConfig.onConnect(ctx -> {
                sessions.put(ctx.sessionId(), ctx);
                log.info("Client connected: {}", ctx.sessionId());
            });
            wsConfig.onMessage(ctx -> {
                log.debug("Message [{}]: {}", ctx.sessionId(), ctx.message());
                // TODO: route messages to game logic
            });
            wsConfig.onClose(ctx -> {
                sessions.remove(ctx.sessionId());
                log.info("Client disconnected: {}", ctx.sessionId());
            });
        });

        app.start(port);
        log.info("Game server listening on :{}", port);
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value != null ? value : fallback;
    }
}
