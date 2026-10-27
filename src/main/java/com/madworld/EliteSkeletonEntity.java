package com.madworld.entity;

import com.madworld.util.MadUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.RangedAttackMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

import java.util.List;

/** Элитный скелет: метко стреляет, но в четверти случаев «случайно» целится в другого моба рядом с целью. */
public class EliteSkeletonEntity extends MadMobEntity implements RangedAttackMob {
    public EliteSkeletonEntity(EntityType<? extends EliteSkeletonEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 40.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.27)
                .add(EntityAttributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected boolean isHostileMob() {
        return true;
    }

    @Override
    public boolean isArmsUp() {
        return isAttacking() || getTarget() != null;
    }

    @Override
    protected double madnessOnDeath() {
        return 3.0;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, danceLock());
        this.goalSelector.add(2, new ProjectileAttackGoal(this, 1.0, 30, 18.0f));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    protected void hurtSound() {
        MadUtil.sound(this, SoundEvents.ENTITY_SKELETON_HURT, 1.0f, 0.9f);
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        if (!(getEntityWorld() instanceof ServerWorld sw)) return;
        LivingEntity aim = target;
        if (MadUtil.rng().nextInt(100) < 25) {
            List<LivingEntity> others = sw.getEntitiesByClass(LivingEntity.class, target.getBoundingBox().expand(10),
                    e -> e != this && e != target && e.isAlive() && !(e instanceof PlayerEntity)
                            && !(e instanceof EliteSkeletonEntity) && !(e instanceof PigEmperorEntity));
            if (!others.isEmpty()) {
                aim = others.get(MadUtil.rng().nextInt(others.size()));
                sw.spawnParticles(ParticleTypes.SMOKE, getX(), getEyeY(), getZ(), 6, 0.2, 0.2, 0.2, 0.01);
            }
        }
        ArrowEntity arrow = new ArrowEntity(sw, this, new ItemStack(Items.ARROW), null);
        double dx = aim.getX() - getX();
        double dy = aim.getBodyY(0.3333) - arrow.getY();
        double dz = aim.getZ() - getZ();
        double h = Math.sqrt(dx * dx + dz * dz);
        arrow.setVelocity(dx, dy + h * 0.2, dz, 1.6f, 14 - sw.getDifficulty().getId() * 4);
        sw.spawnEntity(arrow);
        MadUtil.sound(this, SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f / (MadUtil.rng().nextFloat() * 0.4f + 0.8f));
    }
}
