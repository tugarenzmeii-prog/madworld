package com.madworld.entity;

import com.madworld.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class BananaGrenadeEntity extends ThrownItemEntity {
    public BananaGrenadeEntity(EntityType<? extends BananaGrenadeEntity> type, World world) {
        super(type, world);
    }

    public BananaGrenadeEntity(World world, LivingEntity owner, ItemStack stack) {
        super(ModEntities.BANANA_GRENADE, owner, world, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BANANA_GRENADE;
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (getEntityWorld() instanceof ServerWorld sw && !isRemoved()) {
            BananaEffects.burst(sw, getX(), getY(), getZ(), getOwner());
            discard();
        }
    }
}
