package com.madworld.item;

import com.madworld.util.MadUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Палка телепортации: прыжок до 12 блоков по взгляду в ближайшее безопасное место. */
public class TeleportStickItem extends Item {
    private static final double RANGE = 12.0;

    public TeleportStickItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld sw) || !(user instanceof ServerPlayerEntity p)) return ActionResult.SUCCESS;
        ItemStack stack = user.getStackInHand(hand);
        Vec3d eye = p.getEyePos();
        Vec3d look = p.getRotationVec(1.0f);
        HitResult hit = p.raycast(RANGE, 1.0f, false);
        Vec3d end = hit.getType() == HitResult.Type.MISS ? eye.add(look.multiply(RANGE)) : hit.getPos().subtract(look.multiply(0.6));

        BlockPos dest = null;
        for (int k = 0; k <= 10 && dest == null; k++) {
            BlockPos probe = BlockPos.ofFloored(end.subtract(look.multiply(k)));
            dest = MadUtil.findStandable(sw, probe, 3);
        }
        if (dest == null) {
            p.sendMessage(Text.literal("Здесь негде приземлиться."), true);
            return ActionResult.SUCCESS;
        }
        sw.spawnParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.8, 0.4, 0.3);
        MadUtil.sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        p.requestTeleport(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 40, 0));
        sw.spawnParticles(ParticleTypes.PORTAL, dest.getX() + 0.5, dest.getY() + 1, dest.getZ() + 0.5, 30, 0.4, 0.8, 0.4, 0.3);
        MadUtil.sound(sw, dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5, SoundEvents.ENTITY_ENDERMAN_TELEPORT, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.2f);
        user.getItemCooldownManager().set(stack, 40);
        return ActionResult.SUCCESS;
    }
}
