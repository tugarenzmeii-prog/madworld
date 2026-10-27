package com.madworld.item;

import com.madworld.entity.GoldenPotatoEntity;
import com.madworld.util.MadUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** Золотую картофелину можно метать (3 урона + свечение). */
public class GoldenPotatoItem extends Item {
    public GoldenPotatoItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        MadUtil.sound(world, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5f, 0.6f);
        if (world instanceof ServerWorld sw) {
            ProjectileEntity.spawnWithVelocity(GoldenPotatoEntity::new, sw, stack, user, 0.0f, 1.5f, 1.0f);
        }
        stack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }
}
