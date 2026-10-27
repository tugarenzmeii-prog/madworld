package com.madworld.item;

import com.madworld.entity.BananaGrenadeEntity;
import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** Банановая граната: бросок → облако кожуры → мобы вокруг подпрыгивают 6 секунд. */
public class BananaGrenadeItem extends Item {
    public BananaGrenadeItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        MadUtil.sound(world, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,
                0.5f, 0.4f / (MadUtil.rng().nextFloat() * 0.4f + 0.8f));
        if (world instanceof ServerWorld sw) {
            ProjectileEntity.spawnWithVelocity(BananaGrenadeEntity::new, sw, stack, user, 0.0f, 1.2f, 1.0f);
            if (sw.getServer() != null) MadnessManager.add(sw.getServer(), 0.5);
        }
        stack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }
}
