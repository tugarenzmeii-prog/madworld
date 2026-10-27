package com.madworld.item;

import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import com.madworld.util.Tasks;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.UUID;

/** Магнит для свиней: 15 секунд все свиньи в радиусе 24 блоков (не более 12) бегут к игроку. */
public class PigMagnetItem extends Item {
    public PigMagnetItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld sw) || !(user instanceof ServerPlayerEntity p)) return ActionResult.SUCCESS;
        ItemStack stack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(stack, 900);
        MadUtil.sound(p, SoundEvents.BLOCK_BEACON_ACTIVATE, 1.0f, 1.8f);
        final UUID uuid = p.getUuid();
        Tasks.repeat(300, 5, age -> {
            ServerPlayerEntity owner = sw.getServer() == null ? null : sw.getServer().getPlayerManager().getPlayer(uuid);
            if (owner == null || !owner.isAlive() || owner.getEntityWorld() != sw) return;
            int n = 0;
            for (PigEntity pig : sw.getEntitiesByClass(PigEntity.class, owner.getBoundingBox().expand(24), e -> e.isAlive())) {
                if (n++ >= 12) break;
                if (pig.squaredDistanceTo(owner) > 6.0) pig.getNavigation().startMovingTo(owner, 1.3);
                if (age % 20 == 0) sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pig.getX(), pig.getY() + 0.8, pig.getZ(), 4, 0.3, 0.3, 0.3, 0.05);
            }
        }, null);
        if (sw.getServer() != null) MadnessManager.add(sw.getServer(), 1.0);
        return ActionResult.SUCCESS;
    }
}
