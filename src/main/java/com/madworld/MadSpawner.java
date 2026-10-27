package com.madworld.event;

import com.madworld.config.MadConfig;
import com.madworld.entity.ModEntities;
import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;

/** По мере роста безумия рядом с игроками изредка появляются необычные мобы (с лимитом и сроком жизни). */
public final class MadSpawner {
    private static int timer;

    private MadSpawner() {}

    public static void tick(MinecraftServer server) {
        if (++timer % 600 != 0) return;
        MadConfig cfg = MadConfig.get();
        if (!cfg.naturalMadMobs || !cfg.madnessEnabled) return;
        double f = MadnessManager.fraction();
        if (f < 0.1) return;
        boolean peaceful = server.getOverworld().getDifficulty() == Difficulty.PEACEFUL;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            if (MadUtil.rng().nextDouble() > f * cfg.naturalSpawnChanceAtMax) continue;
            ServerWorld w = MadUtil.world(p);
            BlockPos pos = MadUtil.randomRing(w, p.getBlockPos(), 16, 28);
            if (pos == null) continue;
            double pct = f * 100;
            int roll = MadUtil.rng().nextInt(100);
            EntityType<? extends net.minecraft.entity.mob.MobEntity> type;
            if (roll < 30) type = ModEntities.CREEPER_COURIER;
            else if (roll < 50) type = ModEntities.VILLAGE_MADMAN;
            else if (roll < 70 && pct >= 20) type = ModEntities.NINJA_CHICKEN;
            else if (roll < 88 && pct >= 25 && !peaceful) type = ModEntities.ZOMBIE_DANCER;
            else if (pct >= 40 && !peaceful) type = ModEntities.ELITE_SKELETON;
            else type = ModEntities.CREEPER_COURIER;
            MadUtil.spawnExtra(w, type, pos, 4800);
        }
    }
}
