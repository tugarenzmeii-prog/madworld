package com.madworld.entity;

import com.madworld.item.ModItems;
import com.madworld.util.MadUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** Золотая картофелина: метает Свиной Император (и любой желающий). */
public class GoldenPotatoEntity extends ThrownItemEntity {
    public GoldenPotatoEntity(EntityType<? extends GoldenPotatoEntity> type, World world) {
        super(type, world);
    }

    public GoldenPotatoEntity(World world, LivingEntity owner, ItemStack stack) {
        super(ModEntities.GOLDEN_POTATO, owner, world, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.GOLDEN_POTATO;
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        if (!(getEntityWorld() instanceof ServerWorld sw)) return;
        Entity e = hit.getEntity();
        if (e instanceof LivingEntity le && e != getOwner()) {
            float dmg = getOwner() instanceof PigEmperorEntity ? 5.0f : 3.0f;
            le.damage(sw, sw.getDamageSources().thrown(this, getOwner()), dmg);
            le.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
        }
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (getEntityWorld() instanceof ServerWorld sw && !isRemoved()) {
            sw.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.GOLD_NUGGET)), getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.08);
            MadUtil.sound(sw, getX(), getY(), getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.NEUTRAL, 1.0f, 1.4f);
            discard();
        }
    }
}
