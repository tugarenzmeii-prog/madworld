package com.madworld.util;

import com.madworld.config.MadConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.concurrent.ThreadLocalRandom;

public final class MadUtil {
    public static final String EXTRA_TAG = "madworld_extra";

    private MadUtil() {}

    public static ThreadLocalRandom rng() {
        return ThreadLocalRandom.current();
    }

    public static double rnd(double lo, double hi) {
        return lo + rng().nextDouble() * (hi - lo);
    }

    public static ServerWorld world(Entity e) {
        return (ServerWorld) e.getEntityWorld();
    }

    // --- звуки: перегрузки принимают и SoundEvent, и RegistryEntry<SoundEvent> ---
    public static void sound(World w, double x, double y, double z, SoundEvent s, SoundCategory c, float v, float p) {
        w.playSound(null, x, y, z, s, c, v, p);
    }

    public static void sound(World w, double x, double y, double z, RegistryEntry<SoundEvent> s, SoundCategory c, float v, float p) {
        w.playSound(null, x, y, z, s.value(), c, v, p);
    }

    public static void sound(Entity e, SoundEvent s, float v, float p) {
        sound(e.getEntityWorld(), e.getX(), e.getY(), e.getZ(), s, SoundCategory.NEUTRAL, v, p);
    }

    public static void sound(Entity e, RegistryEntry<SoundEvent> s, float v, float p) {
        sound(e.getEntityWorld(), e.getX(), e.getY(), e.getZ(), s, SoundCategory.NEUTRAL, v, p);
    }

    // --- поиск безопасной точки ---
    public static boolean isStandable(ServerWorld w, BlockPos p) {
        return w.isAir(p) && w.isAir(p.up()) && w.getFluidState(p).isEmpty()
                && w.getBlockState(p.down()).isSideSolidFullSquare(w, p.down(), Direction.UP);
    }

    public static BlockPos findStandable(ServerWorld w, BlockPos around, int vertRange) {
        if (!w.getChunkManager().isChunkLoaded(around.getX() >> 4, around.getZ() >> 4)) return null;
        for (int i = 0; i <= vertRange * 2; i++) {
            int dy = (i % 2 == 0) ? i / 2 : -((i + 1) / 2);
            BlockPos p = around.add(0, dy, 0);
            if (isStandable(w, p)) return p;
        }
        return null;
    }

    public static BlockPos randomRing(ServerWorld w, BlockPos center, int minR, int maxR) {
        for (int i = 0; i < 16; i++) {
            double ang = rnd(0, Math.PI * 2);
            double r = rnd(minR, Math.max(minR, maxR));
            int x = center.getX() + (int) Math.round(Math.cos(ang) * r);
            int z = center.getZ() + (int) Math.round(Math.sin(ang) * r);
            BlockPos p = findStandable(w, new BlockPos(x, center.getY(), z), 8);
            if (p != null) return p;
        }
        return null;
    }

    // --- выпадение предметов ---
    public static ItemEntity drop(ServerWorld w, double x, double y, double z, ItemStack s) {
        ItemEntity e = new ItemEntity(w, x, y, z, s);
        e.setVelocity(rnd(-0.1, 0.1), 0.25, rnd(-0.1, 0.1));
        e.setPickupDelay(10);
        w.spawnEntity(e);
        return e;
    }

    // --- «дополнительные» существа: учёт, лимит и срок жизни ---
    public static int countExtras(ServerWorld w) {
        int n = 0;
        for (Entity e : w.iterateEntities()) {
            if (e instanceof MobEntity && e.getCommandTags().contains(EXTRA_TAG)) n++;
        }
        return n;
    }

    public static boolean canSpawnExtra(ServerWorld w) {
        return countExtras(w) < MadConfig.get().maxExtraMobs;
    }

    public static <T extends Entity> T spawnExtra(ServerWorld w, EntityType<T> type, BlockPos pos, int lifetimeTicks) {
        if (!canSpawnExtra(w)) return null;
        T e = type.spawn(w, pos, SpawnReason.EVENT);
        if (e == null) return null;
        e.addCommandTag(EXTRA_TAG);
        MobTracker.track(e, lifetimeTicks);
        return e;
    }
}
