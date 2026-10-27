package com.madworld.entity;

import com.madworld.util.Advancements;
import com.madworld.util.MadUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/** Курица-ниндзя: быстрая, прыгучая, уворачивается. Крадёт выпавшие предметы, но потом возвращает (или роняет при смерти). */
public class NinjaChickenEntity extends MadMobEntity {
    private ItemStack stolen = ItemStack.EMPTY;
    private int holdTicks;
    private int stealCooldown = 100;

    public NinjaChickenEntity(EntityType<? extends NinjaChickenEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 8.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.42)
                .add(EntityAttributes.FOLLOW_RANGE, 24.0)
                .add(EntityAttributes.STEP_HEIGHT, 1.5)
                .add(EntityAttributes.SAFE_FALL_DISTANCE, 8.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, danceLock());
        this.goalSelector.add(2, new StealGoal());
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.1));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override
    protected void hurtSound() {
        MadUtil.sound(this, SoundEvents.ENTITY_CHICKEN_HURT, 1.0f, 1.5f);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // ниндзя: четверть ударов просто не попадают
        if (source.getAttacker() != null && MadUtil.rng().nextInt(4) == 0) {
            world.spawnParticles(ParticleTypes.POOF, getX(), getY() + 0.4, getZ(), 8, 0.2, 0.2, 0.2, 0.03);
            addVelocity(MadUtil.rnd(-0.5, 0.5), 0.4, MadUtil.rnd(-0.5, 0.5));
            MadUtil.sound(this, SoundEvents.ENTITY_PHANTOM_FLAP, 1.0f, 1.8f);
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getEntityWorld() instanceof ServerWorld sw)) return;
        if (stealCooldown > 0) stealCooldown--;
        if (!stolen.isEmpty()) {
            holdTicks--;
            if (holdTicks % 15 == 0) {
                PlayerEntity p = sw.getClosestPlayer(this, 24);
                if (p != null) {
                    double dx = getX() - p.getX(), dz = getZ() - p.getZ();
                    double d = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                    getNavigation().startMovingTo(getX() + dx / d * 14, getY(), getZ() + dz / d * 14, 1.6);
                }
            }
            if (holdTicks <= 0) {
                MadUtil.drop(sw, getX(), getY() + 0.5, getZ(), stolen);
                for (ServerPlayerEntity p : sw.getPlayers()) {
                    if (squaredDistanceTo(p) < 900) p.sendMessage(Text.literal("§e[Курица-ниндзя] §fЛадно, забирайте. Мне это не нужно."), true);
                }
                stolen = ItemStack.EMPTY;
                stealCooldown = 600;
            }
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!stolen.isEmpty() && getEntityWorld() instanceof ServerWorld sw) {
            MadUtil.drop(sw, getX(), getY() + 0.5, getZ(), stolen);
            stolen = ItemStack.EMPTY;
        }
        super.onDeath(source);
    }

    private class StealGoal extends Goal {
        private ItemEntity item;

        StealGoal() {
            setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (!stolen.isEmpty() || stealCooldown > 0 || !(getEntityWorld() instanceof ServerWorld sw)) return false;
            List<ItemEntity> items = sw.getEntitiesByClass(ItemEntity.class, getBoundingBox().expand(12),
                    ie -> ie.isAlive() && !ie.getStack().isEmpty() && sw.getClosestPlayer(ie, 20) != null);
            if (items.isEmpty()) return false;
            item = items.stream().min(Comparator.comparingDouble(NinjaChickenEntity.this::squaredDistanceTo)).orElse(null);
            return item != null;
        }

        @Override
        public boolean shouldContinue() {
            return stolen.isEmpty() && item != null && item.isAlive();
        }

        @Override
        public void tick() {
            if (age % 5 == 0) getNavigation().startMovingTo(item, 1.5);
            if (squaredDistanceTo(item) < 1.8 && getEntityWorld() instanceof ServerWorld sw) {
                stolen = item.getStack().copy();
                holdTicks = 240;
                item.discard();
                MadUtil.sound(NinjaChickenEntity.this, SoundEvents.ENTITY_ITEM_PICKUP, 1.0f, 1.6f);
                sw.spawnParticles(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 12, 0.2, 0.2, 0.2, 0.02);
                for (ServerPlayerEntity p : sw.getPlayers()) {
                    if (squaredDistanceTo(p) < 400) {
                        p.sendMessage(Text.literal("§e[Курица-ниндзя] §fУкрала: ").append(stolen.getName()).append(Text.literal(" ×" + stolen.getCount())), false);
                        Advancements.grant(p, "chicken_knows");
                    }
                }
            }
        }
    }
}
