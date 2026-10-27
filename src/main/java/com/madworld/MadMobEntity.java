package com.madworld.entity;

import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.List;

/** База для всех новых мобов: танцы, безумие за убийство, исчезновение враждебных мобов в мирном режиме. */
public abstract class MadMobEntity extends PathAwareEntity implements MadAnimated {
    private static final TrackedData<Boolean> DANCING = DataTracker.registerData(MadMobEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final int[] NOTES = {0, 4, 7, 12, 7, 4, 0, 2, 5, 9, 5, 2};

    protected int danceTicks;

    protected MadMobEntity(EntityType<? extends MadMobEntity> type, World world) {
        super(type, world);
    }

    /** Враждебные мобы исчезают в мирной сложности. */
    protected boolean isHostileMob() {
        return false;
    }

    /** Сколько безумия добавляет убийство. */
    protected double madnessOnDeath() {
        return 2.0;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DANCING, false);
    }

    @Override
    public boolean isDancing() {
        return this.dataTracker.get(DANCING);
    }

    @Override
    public void startDance(int ticks) {
        this.danceTicks = Math.max(this.danceTicks, ticks);
        this.dataTracker.set(DANCING, true);
    }

    @Override
    public int getDanceTicks() {
        return this.danceTicks;
    }

    /** Подмешать цель-замок на время танца (вызывать в initGoals с высшим приоритетом). */
    protected Goal danceLock() {
        return new DanceLockGoal<MadMobEntity>(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.getEntityWorld() instanceof ServerWorld sw)) return;
        if (isHostileMob() && sw.getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
            return;
        }
        if (this.danceTicks > 0) {
            this.danceTicks--;
            danceStep(sw, this.danceTicks);
            if (this.danceTicks == 0) this.dataTracker.set(DANCING, false);
        }
    }

    private void danceStep(ServerWorld sw, int left) {
        this.setYaw(this.getYaw() + 22.0f);
        this.setBodyYaw(this.getYaw());
        this.setHeadYaw(this.getYaw());
        if (left % 12 == 0 && this.isOnGround()) this.addVelocity(0, 0.42, 0);
        if (left % 4 == 0) {
            int note = NOTES[(left / 4) % NOTES.length];
            float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
            MadUtil.sound(sw, getX(), getY(), getZ(), SoundEvents.BLOCK_NOTE_BLOCK_BANJO, SoundCategory.NEUTRAL, 1.0f, pitch * 1.5f);
            if (left % 8 == 0) MadUtil.sound(sw, getX(), getY(), getZ(), SoundEvents.BLOCK_NOTE_BLOCK_BASS, SoundCategory.NEUTRAL, 1.0f, pitch);
            sw.spawnParticles(ParticleTypes.NOTE, getX(), getY() + getHeight() + 0.3, getZ(), 1, 0.3, 0.2, 0.3, 1.0);
        }
        // секрет: танец заразителен — соседние мобы мода присоединяются
        if (left % 20 == 0) {
            List<MadMobEntity> near = sw.getEntitiesByClass(MadMobEntity.class, getBoundingBox().expand(7), e -> e != this && e.isAlive() && !(e instanceof PigEmperorEntity));
            if (left > 20) for (MadMobEntity m : near) if (!m.isDancing()) m.startDance(left);
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (this.getEntityWorld() instanceof ServerWorld sw && sw.getServer() != null) {
            MadnessManager.add(sw.getServer(), madnessOnDeath());
        }
    }

    /** Звук получения урона (переопределяется в наследниках). */
    protected void hurtSound() {}

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        boolean r = super.damage(world, source, amount);
        if (r) hurtSound();
        return r;
    }
}
