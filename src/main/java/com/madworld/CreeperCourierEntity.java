package com.madworld.entity;

import com.madworld.item.ModItems;
import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.EnumSet;

/** Крипер-курьер: бегает за игроком и вручает случайный предмет — от алмаза до мусора. Не взрывается. */
public class CreeperCourierEntity extends MadMobEntity {
    private boolean delivered;
    private int leaveTicks = -1;

    public CreeperCourierEntity(EntityType<? extends CreeperCourierEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 16.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, danceLock());
        this.goalSelector.add(2, new DeliverGoal());
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.9));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override
    protected void hurtSound() {
        MadUtil.sound(this, SoundEvents.ENTITY_CREEPER_HURT, 1.0f, 1.3f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getEntityWorld() instanceof ServerWorld sw)) return;
        if (delivered && leaveTicks > 0) {
            leaveTicks--;
            if (leaveTicks % 20 == 0) {
                ServerPlayerEntity p = nearestPlayer(sw, 30);
                if (p != null) {
                    double dx = getX() - p.getX(), dz = getZ() - p.getZ();
                    double d = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                    getNavigation().startMovingTo(getX() + dx / d * 14, getY(), getZ() + dz / d * 14, 1.5);
                }
            }
            if (leaveTicks == 0) {
                sw.spawnParticles(ParticleTypes.POOF, getX(), getY() + 0.8, getZ(), 15, 0.3, 0.5, 0.3, 0.03);
                discard();
            }
        }
    }

    private ServerPlayerEntity nearestPlayer(ServerWorld sw, double range) {
        ServerPlayerEntity best = null;
        double bd = range * range;
        for (ServerPlayerEntity p : sw.getPlayers()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            double d = squaredDistanceTo(p);
            if (d < bd) {
                bd = d;
                best = p;
            }
        }
        return best;
    }

    private static ItemStack gift() {
        int r = MadUtil.rng().nextInt(100);
        if (r < 3) return new ItemStack(Items.DIAMOND);
        if (r < 11) return new ItemStack(Items.EMERALD, 1 + MadUtil.rng().nextInt(3));
        if (r < 21) return new ItemStack(Items.GOLD_INGOT, 1 + MadUtil.rng().nextInt(3));
        if (r < 33) return new ItemStack(Items.IRON_INGOT, 1 + MadUtil.rng().nextInt(4));
        if (r < 43) return new ItemStack(Items.BREAD, 2);
        if (r < 55) return new ItemStack(ModItems.BANANA_PEEL);
        if (r < 63) return new ItemStack(Items.STICK, 1 + MadUtil.rng().nextInt(5));
        if (r < 69) return new ItemStack(Items.DIRT);
        if (r < 74) return new ItemStack(Items.DEAD_BUSH);
        if (r < 80) return new ItemStack(Items.ROTTEN_FLESH, 2);
        if (r < 85) return new ItemStack(Items.LEATHER_BOOTS);
        if (r < 90) {
            ItemStack paper = new ItemStack(Items.PAPER);
            paper.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Квитанция на 0 рублей"));
            return paper;
        }
        if (r < 95) return new ItemStack(ModItems.LUCKY_DIE);
        if (r < 98) return new ItemStack(ModItems.SPEED_SOUP);
        return new ItemStack(ModItems.FORBIDDEN_POTATO);
    }

    private void deliver(ServerWorld sw, ServerPlayerEntity p) {
        ItemEntity ie = MadUtil.drop(sw, getX(), getY() + 0.8, getZ(), gift());
        ie.setVelocity((p.getX() - getX()) * 0.12, 0.3, (p.getZ() - getZ()) * 0.12);
        p.sendMessage(Text.literal("§a[Крипер-курьер] §fВам посылка! Распишитесь вот тут. Нет, не тут."), false);
        MadUtil.sound(this, SoundEvents.ENTITY_CREEPER_PRIMED, 0.6f, 1.6f);
        MadUtil.sound(this, SoundEvents.ENTITY_VILLAGER_YES, 1.0f, 1.2f);
        sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.2, getZ(), 12, 0.4, 0.4, 0.4, 0.05);
        delivered = true;
        leaveTicks = 120;
        if (sw.getServer() != null) MadnessManager.add(sw.getServer(), 1.0);
    }

    private class DeliverGoal extends Goal {
        private ServerPlayerEntity target;

        DeliverGoal() {
            setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (delivered || !(getEntityWorld() instanceof ServerWorld sw)) return false;
            target = nearestPlayer(sw, 40);
            return target != null;
        }

        @Override
        public boolean shouldContinue() {
            return !delivered && target != null && target.isAlive() && squaredDistanceTo(target) < 3600;
        }

        @Override
        public void tick() {
            if (target == null) return;
            getLookControl().lookAt(target, 30.0f, 30.0f);
            if (age % 10 == 0) getNavigation().startMovingTo(target, 1.35);
            if (squaredDistanceTo(target) < 5.0 && getEntityWorld() instanceof ServerWorld sw) deliver(sw, target);
        }
    }
}
