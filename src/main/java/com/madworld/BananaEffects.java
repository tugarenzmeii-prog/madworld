package com.madworld.entity;

import com.madworld.item.ModItems;
import com.madworld.util.MadUtil;
import com.madworld.util.Tasks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Облако банановой кожуры: моб в радиусе 5 блоков 6 секунд смешно подпрыгивает. */
public final class BananaEffects {
    private BananaEffects() {}

    public static void burst(ServerWorld sw, double x, double y, double z, Entity owner) {
        ItemStackParticleEffect peel = new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.YELLOW_DYE));
        sw.spawnParticles(peel, x, y + 0.5, z, 40, 1.2, 0.6, 1.2, 0.08);
        sw.spawnParticles(ParticleTypes.POOF, x, y + 0.3, z, 12, 0.8, 0.3, 0.8, 0.02);
        MadUtil.sound(sw, x, y, z, SoundEvents.BLOCK_SLIME_BLOCK_BREAK, SoundCategory.NEUTRAL, 1.5f, 1.2f);

        List<UUID> ids = new ArrayList<>();
        for (MobEntity m : sw.getEntitiesByClass(MobEntity.class, new Box(x - 5, y - 3, z - 5, x + 5, y + 3, z + 5),
                e -> e.isAlive() && e != owner && !(e instanceof PigEmperorEntity))) {
            if (ids.size() >= 10) break;
            ids.add(m.getUuid());
        }
        MadUtil.drop(sw, x, y + 0.3, z, new ItemStack(ModItems.BANANA_PEEL, 1 + MadUtil.rng().nextInt(2)));
        if (ids.isEmpty()) return;
        Tasks.repeat(120, 8, age -> {
            for (UUID id : ids) {
                Entity e = sw.getEntity(id);
                if (!(e instanceof MobEntity mob) || !mob.isAlive()) continue;
                if (mob.isOnGround()) {
                    mob.addVelocity(MadUtil.rnd(-0.25, 0.25), 0.55, MadUtil.rnd(-0.25, 0.25));
                    if (MadUtil.rng().nextInt(3) == 0) MadUtil.sound(mob, SoundEvents.ENTITY_SLIME_JUMP, 0.8f, (float) MadUtil.rnd(0.9, 1.4));
                }
                sw.spawnParticles(peel, mob.getX(), mob.getY() + 1.0, mob.getZ(), 3, 0.3, 0.3, 0.3, 0.03);
            }
        }, null);
    }
}
