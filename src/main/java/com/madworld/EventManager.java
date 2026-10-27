package com.madworld.event;

import com.madworld.MadWorldMod;
import com.madworld.config.MadConfig;
import com.madworld.madness.MadnessManager;
import com.madworld.util.Advancements;
import com.madworld.util.MadUtil;
import com.madworld.util.Tasks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Планирует и запускает события. Одновременно активно не больше двух (с принудительным запуском — трёх). */
public final class EventManager {
    private static final Map<String, Integer> ACTIVE = new HashMap<>();
    private static final Map<java.util.UUID, Long> LAST_LOG = new HashMap<>();
    private static int countdown = -1;
    private static long ticks;

    private EventManager() {}

    public static void reset() {
        ACTIVE.clear();
        LAST_LOG.clear();
        countdown = -1;
    }

    public static boolean isActive(String id) {
        return ACTIVE.containsKey(id);
    }

    public static void noteLogBroken(ServerPlayerEntity p) {
        LAST_LOG.put(p.getUuid(), ticks);
    }

    public static void tick(MinecraftServer server) {
        ticks++;
        MadConfig cfg = MadConfig.get();
        if (!cfg.eventsEnabled) return;
        if (server.getPlayerManager().getPlayerList().isEmpty()) return;
        if (countdown < 0) countdown = nextInterval();
        if (--countdown <= 0) {
            countdown = nextInterval();
            triggerRandom(server, false);
        }
    }

    private static int nextInterval() {
        MadConfig c = MadConfig.get();
        double secs = MadUtil.rnd(c.minIntervalSeconds, c.maxIntervalSeconds);
        // чем выше безумие, тем чаще события (до -40 %)
        secs *= 1.0 - 0.4 * MadnessManager.fraction();
        return (int) Math.max(200, secs * 20);
    }

    public static boolean triggerRandom(MinecraftServer server, boolean force) {
        double pct = MadnessManager.fraction() * 100.0;
        List<ChaosEvent> pool = new ArrayList<>();
        int total = 0;
        for (ChaosEvent e : ChaosEvents.ALL) {
            if (MadConfig.get().disabledEvents.contains(e.id)) continue;
            if (ACTIVE.containsKey(e.id)) continue;
            if (pct < e.minMadnessPercent) continue;
            if (!e.condition.test(server)) continue;
            pool.add(e);
            total += e.weight;
        }
        if (pool.isEmpty() || total <= 0) return false;
        int r = MadUtil.rng().nextInt(total);
        for (ChaosEvent e : pool) {
            r -= e.weight;
            if (r < 0) return start(server, e, force);
        }
        return false;
    }

    public static boolean start(MinecraftServer server, ChaosEvent ev, boolean force) {
        List<ServerPlayerEntity> ps = new ArrayList<>();
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (p.isAlive() && !p.isSpectator()) ps.add(p);
        }
        if (ps.isEmpty() || ACTIVE.containsKey(ev.id)) return false;
        if (ACTIVE.size() >= (force ? 3 : 2)) return false;
        EventContext ctx = new EventContext(server, ev, ps);
        ACTIVE.put(ev.id, ev.duration);
        try {
            Announcer.announce(ps, ev.title, ev.subtitle);
            ev.onStart.accept(ctx);
        } catch (Throwable t) {
            MadWorldMod.LOGGER.error("Ошибка при запуске события {}", ev.id, t);
            ACTIVE.remove(ev.id);
            return false;
        }
        for (ServerPlayerEntity p : ps) {
            Long lg = LAST_LOG.get(p.getUuid());
            if (lg != null && ticks - lg < 1200) Advancements.grant(p, "just_wanted_wood");
        }
        MadnessManager.add(server, 1.0);
        Tasks.repeat(ev.duration, ev.interval, age -> {
            if (ev.onTick != null) ev.onTick.accept(ctx, age);
        }, () -> {
            try {
                ev.onEnd.accept(ctx);
            } catch (Throwable t) {
                MadWorldMod.LOGGER.error("Ошибка при завершении события {}", ev.id, t);
            } finally {
                ACTIVE.remove(ev.id);
                MadnessManager.eventSurvived(server);
            }
        });
        return true;
    }

    public static ChaosEvent byId(String id) {
        for (ChaosEvent e : ChaosEvents.ALL) if (e.id.equals(id)) return e;
        return null;
    }
}
