package com.scapelike.tick;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TickEngine {
    private static final Logger log = LoggerFactory.getLogger(TickEngine.class);
    private static final long TICK_MS = 600;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong tickCount = new AtomicLong(0);
    private volatile boolean started = false;
    private LongConsumer onTick = seq -> {};

    public void start(LongConsumer onTick) {
        if (started) throw new IllegalStateException("TickEngine already started");
        this.onTick = onTick;
        started = true;
        scheduler.scheduleAtFixedRate(this::tick, 0, TICK_MS, TimeUnit.MILLISECONDS);
        log.info("Tick engine started ({} ms interval)", TICK_MS);
    }

    private void tick() {
        onTick.accept(tickCount.incrementAndGet());
    }

    public void stop() {
        scheduler.shutdown();
        log.info("Tick engine stopped");
    }
}
