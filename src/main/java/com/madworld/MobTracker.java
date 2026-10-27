package com.madworld.util;

import com.madworld.event.EventManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Следит за сроком жизни созданных модом существ и откатывает временные изменения после перезапуска. */
public final class MobTracker {
    public static final String SCALED_TAG = "madworld_scaled";
    public static final String JEB_TAG = "madworld_jeb";

    private static final Map<UUID, Long> EXPIRY = new HashMap<>();
    private static long now;

    private MobTracker() {}

    public static void track(Entity e, int lifetime) {
        EXPIRY.put(e.getUuid(), now + lifetime);
    }

    public static void clear() {
        EXPIRY.clear();
    }

    public static void tick(MinecraftServer server) {
        now++;
        if (now % 20 != 0 || EXPIRY.isEmpty()) return;
        Iterator<Map.Entry<UUID, Long>> it = EXPIRY.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> en = it.next();
            if (now < en.getValue()) continue;
            it.remove();
            for (ServerWorld w : server.getWorlds()) {
                Entity e = w.getEntity(en.getKey());
                if (e != null) {
                    w.spawnParticles(ParticleTypes.POOF, e.getX(), e.getY() + 0.5, e.getZ(), 8, 0.3, 0.3, 0.3, 0.02);
                    e.discard();
                    break;
                }
            }
        }
    }

    public static void onLoad(Entity e) {
        if (e.getCommandTags().contains(MadUtil.EXTRA_TAG) && !EXPIRY.containsKey(e.getUuid())) {
            track(e, 3600);
        }
        if (e.getCommandTags().contains(SCALED_TAG) && !EventManager.isActive("giant_chickens") && !EventManager.isActive("tiny_mobs")) {
            if (e instanceof LivingEntity le) {
                EntityAttributeInstance inst = le.getAttributeInstance(EntityAttributes.SCALE);
                if (inst != null) inst.setBaseValue(1.0);
            }
            e.removeCommandTag(SCALED_TAG);
        }
        if (e.getCommandTags().contains(JEB_TAG) && !EventManager.isActive("rainbow_sheep")) {
            e.setCustomName(null);
            e.removeCommandTag(JEB_TAG);
        }
    }
}
