package com.scapelike.server;

import static com.scapelike.db.generated.Tables.PLAYER;
import static com.scapelike.db.generated.Tables.PLAYER_SESSION;

import com.scapelike.db.Database;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer {
    private static final Logger log = LoggerFactory.getLogger(GameServer.class);

    private final Database db;
    private final Map<String, WsContext> sessions = new ConcurrentHashMap<>();
    private final Map<Long, String> connectedPlayers = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionToPlayer = new ConcurrentHashMap<>();

    public GameServer(Database db) {
        this.db = db;
    }

    public void start() {
        int port = Integer.parseInt(env("PORT", "7070"));
        Javalin app = Javalin.create(
                config -> config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost())));

        app.get("/health", ctx -> ctx.result("OK"));

        app.post("/auth/login", ctx -> {
            record LoginRequest(String username) {}

            LoginRequest request = ctx.bodyAsClass(LoginRequest.class);
            long playerId = findOrCreatePlayer(request.username());
            String token = UUID.randomUUID().toString();

            db.dsl()
                    .insertInto(PLAYER_SESSION)
                    .set(PLAYER_SESSION.TOKEN, token)
                    .set(PLAYER_SESSION.PLAYER_ID, playerId)
                    .set(PLAYER_SESSION.EXPIRES_AT, LocalDateTime.now().plusHours(24))
                    .execute();

            ctx.json(Map.of("token", token));
            log.info("Login: player {} issued token", playerId);
        });

        app.ws("/ws/game", wsConfig -> {
            wsConfig.onConnect(ctx -> {
                String token = ctx.queryParam("token");
                Long playerId = token != null ? validateToken(token) : null;

                if (playerId == null) {
                    ctx.closeSession(4001, "Invalid or expired token");
                    return;
                }

                sessions.put(ctx.sessionId(), ctx);
                connectedPlayers.put(playerId, ctx.sessionId());
                sessionToPlayer.put(ctx.sessionId(), playerId);
                ctx.send(String.format("{\"type\":\"session\",\"playerId\":%d}", playerId));
                log.info("Player {} connected (session {})", playerId, ctx.sessionId());
            });

            wsConfig.onMessage(ctx -> {
                log.debug("Message [{}]: {}", ctx.sessionId(), ctx.message());
                // TODO: route messages to game logic
            });

            wsConfig.onClose(ctx -> {
                sessions.remove(ctx.sessionId());
                Long playerId = sessionToPlayer.remove(ctx.sessionId());
                if (playerId != null) {
                    connectedPlayers.remove(playerId);
                    log.info("Player {} disconnected", playerId);
                }
            });
        });

        app.start(port);
        log.info("Game server listening on :{}", port);
    }

    private long findOrCreatePlayer(String username) {
        var existing =
                db.dsl().selectFrom(PLAYER).where(PLAYER.USERNAME.eq(username)).fetchOne();

        if (existing != null) {
            return existing.getId();
        }

        var created = db.dsl()
                .insertInto(PLAYER)
                .set(PLAYER.USERNAME, username)
                .returning(PLAYER.ID)
                .fetchOne();

        if (created != null) {
            return created.getId();
        }

        // Concurrent insert beat us — re-fetch
        return db.dsl()
                .selectFrom(PLAYER)
                .where(PLAYER.USERNAME.eq(username))
                .fetchOne()
                .getId();
    }

    private Long validateToken(String token) {
        var sessionRecord = db.dsl()
                .selectFrom(PLAYER_SESSION)
                .where(PLAYER_SESSION.TOKEN.eq(token))
                .and(PLAYER_SESSION.EXPIRES_AT.gt(LocalDateTime.now()))
                .fetchOne();

        return sessionRecord == null ? null : sessionRecord.getPlayerId();
    }

    public void broadcastTick(long seq) {
        broadcast(String.format("{\"type\":\"tick\",\"seq\":%d}", seq));
    }

    private void broadcast(String message) {
        sessions.values().forEach(ctx -> {
            try {
                ctx.send(message);
            } catch (Exception e) {
                log.debug("Broadcast failed for session {}: {}", ctx.sessionId(), e.getMessage());
            }
        });
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value != null ? value : fallback;
    }
}
