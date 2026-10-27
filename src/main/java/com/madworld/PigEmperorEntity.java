package com.madworld.entity;

import com.madworld.item.ModItems;
import com.madworld.madness.MadnessManager;
import com.madworld.util.Advancements;
import com.madworld.util.MadUtil;
import com.madworld.util.MobTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Свиной Император — 500 HP, три стадии, полоска здоровья, прыжок с ударной волной,
 * золотые картофелины, свиньи-помощники, реплики при уроне, награда за победу.
 */
public class PigEmperorEntity extends MadMobEntity {
    public static final String MINION_TAG = "madworld_minion";
    private static final int MAX_MINIONS = 8;

    private static final String[] QUIPS = {
            "Как ты смеешь, смертный?!", "Моя корона слишком тяжела для этих ударов!", "Хрю-хрю... то есть, ТРЕПЕЩИ!",
            "Стража! Где моя стража?!", "Я — император! Я не умею проигрывать!", "Ай! Это был мой любимый бок!",
            "Я приказываю тебе перестать!", "Налоги повышаются!", "Ты оскорбил корону!", "Бекон не прощает!"
    };

    private final ServerBossBar bar = new ServerBossBar(Text.literal("Свиной Император").formatted(Formatting.GOLD, Formatting.BOLD),
            BossBar.Color.YELLOW, BossBar.Style.NOTCHED_10);
    private final List<UUID> minions = new ArrayList<>();
    private final Map<UUID, Integer> minionHitCd = new HashMap<>();
    private int phase;
    private boolean leaping;
    private int leapTicks, leapCd = 100, summonCd = 120, potatoCd = 100, speechCd;

    public PigEmperorEntity(EntityType<? extends PigEmperorEntity> type, World world) {
        super(type, world);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 500.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.26)
                .add(EntityAttributes.ATTACK_DAMAGE, 10.0)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(EntityAttributes.SCALE, 2.6)
                .add(EntityAttributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected boolean isHostileMob() {
        return true;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 24.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        bar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bar.removePlayer(player);
    }

    @Override
    protected void hurtSound() {
        MadUtil.sound(this, SoundEvents.ENTITY_PIG_HURT, 2.0f, 0.5f);
    }

    private void speak(ServerWorld sw, String line) {
        for (ServerPlayerEntity p : sw.getPlayers()) {
            if (squaredDistanceTo(p) < 64 * 64) p.sendMessage(Text.literal("§6[Свиной Император] §e" + line), false);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        boolean r = super.damage(world, source, amount);
        if (r && speechCd <= 0 && MadUtil.rng().nextInt(100) < 35) {
            speak(world, QUIPS[MadUtil.rng().nextInt(QUIPS.length)]);
            speechCd = 80;
        }
        return r;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getEntityWorld() instanceof ServerWorld sw)) return;

        float hp = getHealth(), max = getMaxHealth();
        bar.setPercent(Math.max(0f, Math.min(1f, hp / max)));
        int newPhase = hp > max * 0.66f ? 1 : (hp > max * 0.33f ? 2 : 3);
        if (newPhase != phase) {
            if (phase != 0) onPhaseChange(sw, newPhase);
            phase = newPhase;
        }
        if (speechCd > 0) speechCd--;
        if (age % 10 == 0) sw.spawnParticles(ParticleTypes.WAX_ON, getX(), getY() + getHeight(), getZ(), 2, 0.4, 0.2, 0.4, 0.0);
        if (age % 120 == 0) MadUtil.sound(this, SoundEvents.ENTITY_PIG_AMBIENT, 3.0f, 0.5f);

        LivingEntity t = getTarget();

        // --- прыжок + ударная волна ---
        if (leaping) {
            leapTicks++;
            if ((leapTicks > 6 && isOnGround()) || leapTicks > 80) {
                if (isOnGround()) shockwave(sw);
                leaping = false;
                leapCd = phase == 1 ? 200 : (phase == 2 ? 150 : 100);
            }
        } else if (leapCd > 0) {
            leapCd--;
        } else if (t != null && isOnGround() && distanceTo(t) > 3 && distanceTo(t) < 22) {
            startLeap(t);
        }

        // --- золотые картофелины (со 2-й стадии) ---
        if (phase >= 2) {
            if (potatoCd > 0) potatoCd--;
            else if (t != null && canSee(t) && distanceTo(t) > 4 && distanceTo(t) < 24) {
                throwPotato(sw, t);
                potatoCd = phase == 2 ? 90 : 55;
            }
        }

        // --- свиньи-помощники (со 2-й стадии) ---
        if (phase >= 2) {
            if (summonCd > 0) summonCd--;
            else if (t != null && aliveMinions(sw) < MAX_MINIONS) {
                summonPigs(sw);
                summonCd = phase == 2 ? 300 : 200;
            }
        }
        if (age % 10 == 0) tickMinions(sw, t);
    }

    private void onPhaseChange(ServerWorld sw, int newPhase) {
        EntityAttributeInstance sp = getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (sp != null) sp.setBaseValue(0.26 + 0.04 * (newPhase - 1));
        bar.setColor(newPhase == 2 ? BossBar.Color.RED : BossBar.Color.PURPLE);
        MadUtil.sound(this, SoundEvents.ENTITY_PIG_AMBIENT, 4.0f, 0.3f);
        sw.spawnParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1, getZ(), 3, 1.0, 0.5, 1.0, 0.0);
        speak(sw, newPhase == 2 ? "Довольно церемоний! Свиньи, ко мне!" : "ВЫ ЗАСТАВИЛИ МЕНЯ ВСТАТЬ С ТРОНА!");
    }

    private void startLeap(LivingEntity t) {
        double dx = t.getX() - getX(), dz = t.getZ() - getZ();
        double d = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
        getNavigation().stop();
        setVelocity(dx / d * 0.85, 0.95, dz / d * 0.85);
        leaping = true;
        leapTicks = 0;
        MadUtil.sound(this, SoundEvents.ENTITY_PIG_AMBIENT, 4.0f, 0.4f);
    }

    private void shockwave(ServerWorld sw) {
        float dmg = 5.0f + phase * 2.0f;
        for (PlayerEntity p : sw.getEntitiesByClass(PlayerEntity.class, getBoundingBox().expand(7, 3, 7), e -> e.isAlive() && !e.isSpectator())) {
            if (!p.isOnGround()) continue; // можно перепрыгнуть
            p.damage(sw, sw.getDamageSources().mobAttack(this), dmg);
            p.takeKnockback(1.4, getX() - p.getX(), getZ() - p.getZ());
        }
        for (int i = 0; i < 24; i++) {
            double a = i / 24.0 * Math.PI * 2;
            for (double r = 2; r <= 7; r += 2.5) {
                sw.spawnParticles(ParticleTypes.POOF, getX() + Math.cos(a) * r, getY() + 0.1, getZ() + Math.sin(a) * r, 2, 0.1, 0.05, 0.1, 0.02);
            }
        }
        sw.spawnParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.3, getZ(), 4, 1.5, 0.1, 1.5, 0.0);
        MadUtil.sound(sw, getX(), getY(), getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 3.0f, 0.6f);
    }

    private void throwPotato(ServerWorld sw, LivingEntity t) {
        GoldenPotatoEntity pot = new GoldenPotatoEntity(sw, this, new ItemStack(ModItems.GOLDEN_POTATO));
        double dx = t.getX() - getX(), dz = t.getZ() - getZ();
        double dy = t.getBodyY(0.3333) - pot.getY();
        double h = Math.sqrt(dx * dx + dz * dz);
        pot.setVelocity(dx, dy + h * 0.2, dz, 1.4f, 1.0f);
        sw.spawnEntity(pot);
        MadUtil.sound(this, SoundEvents.ENTITY_SNOWBALL_THROW, 2.0f, 0.5f);
    }

    private int aliveMinions(ServerWorld sw) {
        int n = 0;
        Iterator<UUID> it = minions.iterator();
        while (it.hasNext()) {
            Entity e = sw.getEntity(it.next());
            if (e == null || !e.isAlive()) it.remove();
            else n++;
        }
        return n;
    }

    private void summonPigs(ServerWorld sw) {
        int spawned = 0;
        for (int i = 0; i < 3 && aliveMinions(sw) < MAX_MINIONS; i++) {
            BlockPos base = getBlockPos().add(MadUtil.rng().nextInt(11) - 5, 0, MadUtil.rng().nextInt(11) - 5);
            BlockPos pos = MadUtil.findStandable(sw, base, 4);
            if (pos == null) continue;
            PigEntity pig = EntityType.PIG.spawn(sw, pos, SpawnReason.MOB_SUMMONED);
            if (pig == null) continue;
            pig.addCommandTag(MINION_TAG);
            pig.addCommandTag(MadUtil.EXTRA_TAG);
            MobTracker.track(pig, 3600);
            minions.add(pig.getUuid());
            spawned++;
            sw.spawnParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.02);
        }
        if (spawned > 0) {
            speak(sw, "Ко мне, мои верные подданные!");
            MadUtil.sound(sw, getX(), getY(), getZ(), SoundEvents.BLOCK_BELL_USE, SoundCategory.HOSTILE, 3.0f, 0.8f);
        }
    }

    /** Помощники бегут на цель и таранят её. */
    private void tickMinions(ServerWorld sw, LivingEntity t) {
        Iterator<UUID> it = minions.iterator();
        while (it.hasNext()) {
            UUID id = it.next();
            Entity e = sw.getEntity(id);
            if (!(e instanceof PigEntity pig) || !pig.isAlive()) {
                it.remove();
                continue;
            }
            int cd = minionHitCd.getOrDefault(id, 0);
            if (cd > 0) minionHitCd.put(id, cd - 10);
            if (t == null) continue;
            pig.getNavigation().startMovingTo(t, 1.4);
            if (pig.squaredDistanceTo(t) < 2.6 && cd <= 0) {
                t.damage(sw, sw.getDamageSources().mobAttack(this), 3.0f);
                t.takeKnockback(0.6, pig.getX() - t.getX(), pig.getZ() - t.getZ());
                MadUtil.sound(pig, SoundEvents.ENTITY_PIG_AMBIENT, 1.0f, 1.6f);
                minionHitCd.put(id, 40);
            }
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!(getEntityWorld() instanceof ServerWorld sw)) return;
        bar.clearPlayers();
        speak(sw, "Это... невозможно... Моя корона...");
        double x = getX(), y = getY() + 0.5, z = getZ();
        MadUtil.drop(sw, x, y, z, new ItemStack(ModItems.EMPEROR_CROWN));
        MadUtil.drop(sw, x, y, z, new ItemStack(ModItems.GOLDEN_TOILET));
        MadUtil.drop(sw, x, y, z, new ItemStack(ModItems.GOLDEN_POTATO, 8 + MadUtil.rng().nextInt(9)));
        MadUtil.drop(sw, x, y, z, new ItemStack(Items.DIAMOND, 8 + MadUtil.rng().nextInt(9)));
        MadUtil.drop(sw, x, y, z, new ItemStack(Items.NETHERITE_INGOT, 1));
        MadUtil.drop(sw, x, y, z, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2));
        MadUtil.drop(sw, x, y, z, new ItemStack(ModItems.LUCKY_DIE, 3));
        MadUtil.drop(sw, x, y, z, new ItemStack(ModItems.CHAOS_REMOTE, 1));
        ExperienceOrbEntity.spawn(sw, new Vec3d(getX(), getY(), getZ()), 500);
        for (UUID id : minions) {
            Entity e = sw.getEntity(id);
            if (e != null) {
                sw.spawnParticles(ParticleTypes.POOF, e.getX(), e.getY() + 0.5, e.getZ(), 8, 0.3, 0.3, 0.3, 0.02);
                e.discard();
            }
        }
        minions.clear();
        for (ServerPlayerEntity p : sw.getPlayers()) {
            if (squaredDistanceTo(p) < 64 * 64) Advancements.grant(p, "pig_king");
        }
        if (sw.getServer() != null) MadnessManager.add(sw.getServer(), 25.0);
    }
}
