package com.scapelike.tick;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TickEngine {
    private static final Logger log = LoggerFactory.getLogger(TickEngine.class);
    private static final long TICK_MS = 600;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, 0, TICK_MS, TimeUnit.MILLISECONDS);
        log.info("Tick engine started ({} ms interval)", TICK_MS);
    }

    private void tick() {
        // TODO: process game state each tick
    }

    public void stop() {
        scheduler.shutdown();
        log.info("Tick engine stopped");
    }
}
