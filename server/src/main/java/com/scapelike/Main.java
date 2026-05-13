package com.scapelike;

import com.scapelike.db.Database;
import com.scapelike.server.GameServer;
import com.scapelike.tick.TickEngine;

public class Main {
    public static void main(String[] args) {
        TickEngine tickEngine = new TickEngine();
        Runtime.getRuntime().addShutdownHook(new Thread(tickEngine::stop));

        Database db = new Database();
        GameServer server = new GameServer(db);

        tickEngine.start(server::broadcastTick);
        server.start();
    }
}
