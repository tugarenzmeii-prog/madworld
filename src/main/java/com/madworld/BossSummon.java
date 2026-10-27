package com.madworld.event;

import com.madworld.config.MadConfig;
import com.madworld.entity.ModEntities;
import com.madworld.entity.PigEmperorEntity;
import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import com.madworld.util.Tasks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

/**
 * Свиной Император появляется только если выполнены ВСЕ условия:
 * безумие выше порога, в мире нет другого императора, сложность не «мирная»,
 * прошло не менее 20 минут с прошлого вызова, игрок в Верхнем мире под открытым небом.
 */
public final class BossSummon {
    private static long lastSummon = -1_000_000L;
    private static boolean pending;

    private BossSummon() {}

    public static boolean exists(MinecraftServer s) {
        for (ServerWorld w : s.getWorlds()) {
            for (Entity e : w.iterateEntities()) {
                if (e instanceof PigEmperorEntity && e.isAlive()) return true;
            }
        }
        return false;
    }

    public static boolean canSummon(MinecraftServer s) {
        return reason(s) == null;
    }

    /** null, если вызвать можно; иначе причина отказа. */
    public static String reason(MinecraftServer s) {
        if (pending) return "Император уже в пути.";
        if (MadnessManager.fraction() * 100.0 < MadConfig.get().bossMadnessPercent) return "Мир ещё недостаточно безумен.";
        if (s.getOverworld().getDifficulty() == Difficulty.PEACEFUL) return "Император не приходит в мирный режим.";
        if (exists(s)) return "Император уже существует.";
        if (s.getTicks() - lastSummon < 24000) return "Император ещё не отдохнул после прошлого визита.";
        return null;
    }

    /** Обряд: запускает отсчёт в 10 секунд, затем появляется босс. */
    public static boolean begin(ServerPlayerEntity p) {
        MinecraftServer server = p.getEntityWorld().getServer();
        if (server == null || !canSummon(server)) return false;
        ServerWorld w = MadUtil.world(p);
        if (w.getRegistryKey() != World.OVERWORLD || !w.isSkyVisible(p.getBlockPos())) {
            p.sendMessage(Text.literal("Император явится только в Верхнем мире, под открытым небом."), true);
            return false;
        }
        BlockPos pos = MadUtil.randomRing(w, p.getBlockPos(), 14, 20);
        if (pos == null) {
            p.sendMessage(Text.literal("Не нашлось места для трона. Попробуйте на открытой местности."), true);
            return false;
        }
        pending = true;
        lastSummon = server.getTicks();
        Announcer.announce(server.getPlayerManager().getPlayerList(), "ЗЕМЛЯ ДРОЖИТ...", "Свиной Император прибудет через 10 секунд");
        Tasks.repeat(200, 5, age -> {
            w.spawnParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 12, 1.2, 0.2, 1.2, 0.02);
            w.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5, 4, 0.5, 2.0, 0.5, 0.02);
            if (age % 40 == 0) MadUtil.sound(w, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_BELL_USE, SoundCategory.HOSTILE, 4.0f, 0.6f);
        }, () -> {
            try {
                PigEmperorEntity boss = ModEntities.PIG_EMPEROR.spawn(w, pos, SpawnReason.TRIGGERED);
                if (boss != null) {
                    boss.setPersistent();
                    LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w, SpawnReason.EVENT);
                    if (bolt != null) {
                        bolt.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                        bolt.setCosmetic(true);
                        w.spawnEntity(bolt);
                    }
                    for (ServerPlayerEntity pl : server.getPlayerManager().getPlayerList()) {
                        pl.sendMessage(Text.literal("§6[Свиной Император] §eПадите ниц, жалкие смертные!"), false);
                    }
                }
            } finally {
                pending = false;
            }
        });
        return true;
    }

    /** Обряд через Пульт хаоса (Shift + ПКМ). Возвращает true, если обряд начат. */
    public static boolean ritual(ServerPlayerEntity p) {
        MinecraftServer server = p.getEntityWorld().getServer();
        if (server == null) return false;
        String r = reason(server);
        if (r != null) {
            p.sendMessage(Text.literal(r), true);
            return false;
        }
        return begin(p);
    }
}
