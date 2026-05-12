package com.scapelike.tick;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TickEngine {
    private static final long TICK_MS = 600;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, 0, TICK_MS, TimeUnit.MILLISECONDS);
    }

    private void tick() {
        // TODO: process game state each tick
    }

    public void stop() {
        scheduler.shutdown();
    }
}
