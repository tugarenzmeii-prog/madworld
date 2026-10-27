package com.madworld.item;

import com.madworld.entity.ModEntities;
import com.madworld.entity.PigEmperorEntity;
import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

/** Логика Молотка судьбы: 30 % ударов превращают моба в другого. Вызывается из AttackEntityCallback. */
public final class HammerOfFate {
    private record Option(EntityType<? extends MobEntity> type, int weight) {}

    private static final Option[] POOL = {
            new Option(EntityType.CHICKEN, 30), new Option(EntityType.PIG, 25), new Option(EntityType.COW, 15),
            new Option(EntityType.SHEEP, 10), new Option(EntityType.RABBIT, 8), new Option(EntityType.MOOSHROOM, 4),
            new Option(ModEntities.NINJA_CHICKEN, 4), new Option(ModEntities.CREEPER_COURIER, 4)
    };

    private HammerOfFate() {}

    public static void onAttack(PlayerEntity player, ServerWorld sw, Entity target) {
        if (!(target instanceof MobEntity mob) || !mob.isAlive()) return;
        if (mob instanceof PigEmperorEntity || mob.getMaxHealth() > 120) return;
        ItemStack hammer = player.getMainHandStack();
        if (player.getItemCooldownManager().isCoolingDown(hammer)) return;
        player.getItemCooldownManager().set(hammer, 15);
        if (MadUtil.rng().nextInt(100) >= 30) return;

        int total = 0;
        for (Option o : POOL) total += o.weight();
        int r = MadUtil.rng().nextInt(total);
        EntityType<? extends MobEntity> chosen = POOL[0].type();
        for (Option o : POOL) {
            r -= o.weight();
            if (r < 0) {
                chosen = o.type();
                break;
            }
        }
        if (chosen == mob.getType()) return;
        MobEntity n = chosen.spawn(sw, mob.getBlockPos(), SpawnReason.TRIGGERED);
        if (n == null) return;
        n.setPosition(mob.getX(), mob.getY(), mob.getZ());
        n.setYaw(mob.getYaw());
        n.setCustomName(mob.getCustomName());
        if (mob.isBaby()) n.setBaby(true);
        mob.discard();
        sw.spawnParticles(ParticleTypes.POOF, n.getX(), n.getY() + 0.6, n.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
        sw.spawnParticles(ParticleTypes.ENCHANT, n.getX(), n.getY() + 1.0, n.getZ(), 20, 0.4, 0.6, 0.4, 0.5);
        MadUtil.sound(n, SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.2f);
        if (sw.getServer() != null) MadnessManager.add(sw.getServer(), 1.0);
    }
}
