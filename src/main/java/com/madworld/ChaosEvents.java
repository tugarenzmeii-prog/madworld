package com.madworld.event;

import com.madworld.entity.ModEntities;
import com.madworld.entity.PigEmperorEntity;
import com.madworld.item.ModItems;
import com.madworld.net.ModNetwork;
import com.madworld.util.MadUtil;
import com.madworld.util.MobTracker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/** Все безумные события мира (25 штук). */
public final class ChaosEvents {
    public static final List<ChaosEvent> ALL = new ArrayList<>();

    private static final Item[] JUNK = {Items.FEATHER, Items.STRING, Items.BONE, Items.DIRT, Items.POPPY, Items.STICK,
            Items.ROTTEN_FLESH, Items.SNOWBALL, Items.COOKIE, Items.WHEAT_SEEDS, Items.APPLE, Items.BREAD, Items.PUMPKIN_SEEDS};

    private ChaosEvents() {}

    private static void reg(ChaosEvent e) {
        ALL.add(e);
    }

    private static boolean notPeaceful(net.minecraft.server.MinecraftServer s) {
        return s.getOverworld().getDifficulty() != Difficulty.PEACEFUL;
    }

    private static void effect(LivingEntity e, RegistryEntry<StatusEffect> eff, int ticks, int amp) {
        e.addStatusEffect(new StatusEffectInstance(eff, ticks, amp));
    }

    private static void setScale(LivingEntity e, double v) {
        EntityAttributeInstance inst = e.getAttributeInstance(EntityAttributes.SCALE);
        if (inst != null) {
            inst.setBaseValue(v);
            if (v != 1.0) e.addCommandTag(MobTracker.SCALED_TAG); else e.removeCommandTag(MobTracker.SCALED_TAG);
        }
    }

    private static <T extends MobEntity> void spawnAround(EventContext c, EntityType<T> type, int per, int life, int minR, int maxR, Consumer<T> init) {
        for (ServerPlayerEntity p : c.players()) {
            ServerWorld w = MadUtil.world(p);
            for (int i = 0; i < per; i++) {
                BlockPos pos = MadUtil.randomRing(w, p.getBlockPos(), minR, maxR);
                if (pos == null) continue;
                T e = MadUtil.spawnExtra(w, type, pos, life);
                if (e == null) return; // достигнут лимит
                c.entities.add(e.getUuid());
                if (init != null) init.accept(e);
            }
        }
    }

    private static void particlesGold(ServerWorld w, Entity e) {
        w.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.GOLD_NUGGET)), e.getX(), e.getY() + 1.0, e.getZ(), 6, 0.3, 0.4, 0.3, 0.05);
    }

    static {
        // 1
        reg(ChaosEvent.of("gravity", "ГРАВИТАЦИЯ УШЛА В ОТПУСК!", "Прыгайте, пока она не вернулась").weight(10).duration(300).interval(20)
                .onStart(c -> c.players().forEach(p -> {
                    effect(p, StatusEffects.JUMP_BOOST, 320, 5);
                    effect(p, StatusEffects.SLOW_FALLING, 420, 0);
                }))
                .onTick((c, age) -> {
                    for (ServerPlayerEntity p : c.players()) {
                        ServerWorld w = MadUtil.world(p);
                        int n = 0;
                        for (MobEntity m : w.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(20), Entity::isAlive)) {
                            if (n++ >= 12) break;
                            effect(m, StatusEffects.SLOW_FALLING, 100, 0);
                            if (m.isOnGround() && MadUtil.rng().nextBoolean())
                                m.addVelocity(MadUtil.rnd(-0.15, 0.15), 0.7, MadUtil.rnd(-0.15, 0.15));
                        }
                    }
                }).build());
        // 2
        reg(ChaosEvent.of("chicken_epidemic", "КУРИНАЯ ЭПИДЕМИЯ!", "Ко-ко-ко-ко-ко!").weight(10).duration(60)
                .onStart(c -> spawnAround(c, EntityType.CHICKEN, 7, 1200, 3, 8, ch -> {
                    if (MadUtil.rng().nextInt(4) == 0) ch.setBaby(true);
                    MadUtil.sound(ch, SoundEvents.ENTITY_CHICKEN_AMBIENT, 1.0f, 1.0f);
                })).build());
        // 3
        reg(ChaosEvent.of("sky_falling", "НЕБО ПАДАЕТ!", "Не волнуйтесь, это безобидно").weight(9).duration(200).interval(5)
                .onTick((c, age) -> {
                    for (ServerPlayerEntity p : c.players()) {
                        ServerWorld w = MadUtil.world(p);
                        for (int i = 0; i < 2; i++) {
                            Item it = JUNK[MadUtil.rng().nextInt(JUNK.length)];
                            if (MadUtil.rng().nextInt(25) == 0) it = ModItems.BANANA_PEEL;
                            ItemEntity e = new ItemEntity(w, p.getX() + MadUtil.rnd(-6, 6), p.getY() + 16, p.getZ() + MadUtil.rnd(-6, 6), new ItemStack(it, 1 + MadUtil.rng().nextInt(2)));
                            e.setPickupDelay(10);
                            w.spawnEntity(e);
                            MobTracker.track(e, 600);
                        }
                    }
                }).build());
        // 4
        reg(ChaosEvent.of("reversed_controls", "ДЕНЬ ПЕРЕВЁРНУТОГО УПРАВЛЕНИЯ!", "W и S поменялись местами. A и D — тоже").weight(7).duration(400)
                .onStart(c -> c.players().forEach(p -> ServerPlayNetworking.send(p, new ModNetwork.ControlsPayload(400))))
                .onEnd(c -> c.players().forEach(p -> {
                    ServerPlayNetworking.send(p, new ModNetwork.ControlsPayload(0));
                    p.sendMessage(Text.literal("Управление снова нормальное. Наверное."), true);
                })).build());
        // 5
        reg(ChaosEvent.of("rich_pigs", "ВСЕ СВИНЬИ СТАЛИ БОГАТЫМИ!", "Они делятся. Иногда").weight(8).duration(300).interval(40)
                .onStart(c -> {
                    int found = 0;
                    for (ServerPlayerEntity p : c.players()) {
                        ServerWorld w = MadUtil.world(p);
                        for (PigEntity pig : w.getEntitiesByClass(PigEntity.class, p.getBoundingBox().expand(40), Entity::isAlive)) {
                            if (c.entities.size() >= 8) break;
                            c.entities.add(pig.getUuid());
                            found++;
                        }
                    }
                    if (found < 3) spawnAround(c, EntityType.PIG, 3, 2400, 4, 9, null);
                })
                .onTick((c, age) -> {
                    for (UUID id : c.entities) {
                        Entity e = c.find(id);
                        if (!(e instanceof PigEntity pig) || !pig.isAlive()) continue;
                        ServerWorld w = MadUtil.world(pig);
                        particlesGold(w, pig);
                        if (MadUtil.rng().nextInt(10) < 8) {
                            MadUtil.drop(w, pig.getX(), pig.getY() + 0.8, pig.getZ(), Rewards.pigGift());
                            MadUtil.sound(pig, SoundEvents.ENTITY_PIG_AMBIENT, 1.0f, 1.4f);
                        }
                    }
                }).build());
        // 6
        reg(ChaosEvent.of("offended_blocks", "БЛОКИ ОБИДЕЛИСЬ!", "Они дрожат от возмущения").weight(8).duration(240).interval(4)
                .onTick((c, age) -> {
                    for (ServerPlayerEntity p : c.players()) {
                        ServerWorld w = MadUtil.world(p);
                        for (int i = 0; i < 5; i++) {
                            BlockPos b = p.getBlockPos().add(MadUtil.rng().nextInt(13) - 6, MadUtil.rng().nextInt(7) - 3, MadUtil.rng().nextInt(13) - 6);
                            if (w.isAir(b) || !w.isAir(b.up())) continue;
                            w.spawnParticles(ParticleTypes.ANGRY_VILLAGER, b.getX() + 0.5, b.getY() + 1.1, b.getZ() + 0.5, 1, 0.2, 0.1, 0.2, 0);
                            w.spawnParticles(ParticleTypes.CRIT, b.getX() + 0.5, b.getY() + 1.0, b.getZ() + 0.5, 4, 0.3, 0.05, 0.3, 0.05);
                            if (age % 12 == 0) MadUtil.sound(w, b.getX() + 0.5, b.getY(), b.getZ() + 0.5, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS, 0.8f, (float) MadUtil.rnd(0.6, 1.4));
                            if (age % 60 == 0) MadUtil.sound(w, b.getX() + 0.5, b.getY(), b.getZ() + 0.5, SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.BLOCKS, 0.6f, 0.7f);
                        }
                    }
                })
                .onEnd(c -> c.players().forEach(p -> p.sendMessage(Text.literal("Блоки вас простили. Пока."), true))).build());
        // 7
        reg(ChaosEvent.of("space_delivery", "КОСМИЧЕСКАЯ ДОСТАВКА!", "Сундук с сюрпризом падает с неба").weight(6).duration(100).interval(2)
                .onStart(c -> {
                    ServerPlayerEntity p = c.randomPlayer();
                    if (p == null) return;
                    ServerWorld w = MadUtil.world(p);
                    BlockPos pos = MadUtil.randomRing(w, p.getBlockPos(), 10, 18);
                    if (pos == null) return;
                    c.pos = pos;
                    c.posWorld = w;
                    c.players().forEach(pl -> pl.sendMessage(Text.literal("Посылка приземлится около X: " + pos.getX() + " Z: " + pos.getZ()), false));
                })
                .onTick((c, age) -> {
                    if (c.pos == null) return;
                    double y = c.pos.getY() + 40 - age * 0.4;
                    c.posWorld.spawnParticles(ParticleTypes.FIREWORK, c.pos.getX() + 0.5, y, c.pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.02);
                    c.posWorld.spawnParticles(ParticleTypes.FLAME, c.pos.getX() + 0.5, y, c.pos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.01);
                    if (age == 2) MadUtil.sound(c.posWorld, c.pos.getX(), c.pos.getY(), c.pos.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 4.0f, 0.6f);
                })
                .onEnd(c -> {
                    if (c.pos == null) return;
                    ServerWorld w = c.posWorld;
                    if (!w.isAir(c.pos)) return;
                    w.setBlockState(c.pos, Blocks.CHEST.getDefaultState());
                    BlockEntity be = w.getBlockEntity(c.pos);
                    if (be instanceof ChestBlockEntity chest) {
                        int n = 4 + MadUtil.rng().nextInt(4);
                        for (int i = 0; i < n; i++) chest.setStack(MadUtil.rng().nextInt(chest.size()), Rewards.chestReward());
                    }
                    w.spawnParticles(ParticleTypes.POOF, c.pos.getX() + 0.5, c.pos.getY() + 0.5, c.pos.getZ() + 0.5, 30, 0.5, 0.5, 0.5, 0.05);
                    MadUtil.sound(w, c.pos.getX(), c.pos.getY(), c.pos.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 1.5f, 1.2f);
                }).build());
        // 8
        reg(ChaosEvent.of("ducklings", "ВТОРЖЕНИЕ УТЕНОЧКОВ!", "Они очень милые. Они убегают").weight(8).duration(1200).interval(10)
                .onStart(c -> spawnAround(c, EntityType.CHICKEN, 8, 1800, 3, 8, ch -> {
                    ch.setBaby(true);
                    ch.setCustomName(Text.literal("Утёнок"));
                    ch.setCustomNameVisible(false);
                }))
                .onTick((c, age) -> {
                    for (UUID id : c.entities) {
                        Entity e = c.find(id);
                        if (!(e instanceof MobEntity m) || !m.isAlive()) continue;
                        for (ServerPlayerEntity p : c.players()) {
                            double dx = m.getX() - p.getX(), dz = m.getZ() - p.getZ();
                            double d = Math.sqrt(dx * dx + dz * dz);
                            if (d < 6 && p.getEntityWorld() == m.getEntityWorld()) {
                                if (d < 0.01) { dx = 1; dz = 0; d = 1; }
                                m.getNavigation().startMovingTo(m.getX() + dx / d * 8, m.getY(), m.getZ() + dz / d * 8, 1.5);
                                break;
                            }
                        }
                    }
                }).build());
        // 9
        reg(ChaosEvent.of("dance_party", "ТАНЦЫ БЕЗ ТОРМОЗОВ!", "Зомби-танцоры уже в пути").weight(6).minMadness(15).duration(40).condition(ChaosEvents::notPeaceful)
                .onStart(c -> spawnAround(c, ModEntities.ZOMBIE_DANCER, 2, 2400, 8, 14, z -> z.startDance(160))).build());
        // 10
        reg(ChaosEvent.of("courier_delivery", "КРИПЕР-КУРЬЕР ВЫЕЗЖАЕТ!", "У него для вас посылка. Честно").weight(7).minMadness(8).duration(40)
                .onStart(c -> spawnAround(c, ModEntities.CREEPER_COURIER, 1, 3600, 14, 22, null)).build());
        // 11
        reg(ChaosEvent.of("rainbow_sheep", "РАДУЖНОЕ СТАДО!", "Все овцы переливаются").weight(6).duration(800)
                .onStart(c -> {
                    int found = 0;
                    for (ServerPlayerEntity p : c.players()) {
                        for (SheepEntity s : MadUtil.world(p).getEntitiesByClass(SheepEntity.class, p.getBoundingBox().expand(40), Entity::isAlive)) {
                            if (c.entities.size() >= 20) break;
                            s.setCustomName(Text.literal("jeb_"));
                            s.setCustomNameVisible(false);
                            s.addCommandTag(MobTracker.JEB_TAG);
                            c.entities.add(s.getUuid());
                            found++;
                        }
                    }
                    if (found == 0) spawnAround(c, EntityType.SHEEP, 3, 2400, 4, 9, s -> {
                        s.setCustomName(Text.literal("jeb_"));
                        s.setCustomNameVisible(false);
                        s.addCommandTag(MobTracker.JEB_TAG);
                    });
                })
                .onEnd(c -> {
                    for (UUID id : c.entities) {
                        Entity e = c.find(id);
                        if (e != null && e.getCommandTags().contains(MobTracker.JEB_TAG)) {
                            e.setCustomName(null);
                            e.removeCommandTag(MobTracker.JEB_TAG);
                        }
                    }
                }).build());
        // 12
        reg(ChaosEvent.of("giant_chickens", "КУРИЦЫ ВЫРОСЛИ!", "Кто-то подмешал им удобрение").weight(6).duration(400)
                .onStart(c -> {
                    int found = 0;
                    for (ServerPlayerEntity p : c.players()) {
                        for (ChickenEntity ch : MadUtil.world(p).getEntitiesByClass(ChickenEntity.class, p.getBoundingBox().expand(40), Entity::isAlive)) {
                            if (c.entities.size() >= 10) break;
                            setScale(ch, 3.0);
                            c.entities.add(ch.getUuid());
                            found++;
                        }
                    }
                    if (found == 0) spawnAround(c, EntityType.CHICKEN, 3, 1200, 5, 10, ch -> {
                        setScale(ch, 3.0);
                    });
                })
                .onEnd(c -> {
                    for (UUID id : c.entities) {
                        if (c.find(id) instanceof LivingEntity le) setScale(le, 1.0);
                    }
                }).build());
        // 13
        reg(ChaosEvent.of("tiny_mobs", "ВСЕ СТАЛИ КРОШЕЧНЫМИ!", "Даже самые страшные").weight(6).duration(400)
                .onStart(c -> {
                    for (ServerPlayerEntity p : c.players()) {
                        for (MobEntity m : MadUtil.world(p).getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(30), Entity::isAlive)) {
                            if (c.entities.size() >= 12) break;
                            if (m instanceof PigEmperorEntity || m.getMaxHealth() > 100) continue;
                            setScale(m, 0.35);
                            c.entities.add(m.getUuid());
                        }
                    }
                })
                .onEnd(c -> {
                    for (UUID id : c.entities) {
                        if (c.find(id) instanceof LivingEntity le) setScale(le, 1.0);
                    }
                }).build());
        // 14
        reg(ChaosEvent.of("potato_gift", "КАРТОШКА ДЛЯ ВСЕХ!", "Бесплатно. Без регистрации").weight(8).duration(40)
                .onStart(c -> {
                    for (ServerPlayerEntity p : c.players()) {
                        p.getInventory().offerOrDrop(new ItemStack(MadUtil.rng().nextBoolean() ? Items.POTATO : Items.BAKED_POTATO, 2 + MadUtil.rng().nextInt(3)));
                        if (MadUtil.rng().nextInt(100) < 6) p.getInventory().offerOrDrop(new ItemStack(ModItems.FORBIDDEN_POTATO));
                        MadUtil.sound(p, SoundEvents.ENTITY_ITEM_PICKUP, 1.0f, 0.8f);
                    }
                }).build());
        // 15
        reg(ChaosEvent.of("sound_mess", "ЗВУКОВАЯ ПУТАНИЦА!", "Кто-то перепутал все звуки").weight(8).duration(240).interval(8)
                .onTick((c, age) -> {
                    for (ServerPlayerEntity p : c.players()) {
                        ServerWorld w = MadUtil.world(p);
                        double x = p.getX() + MadUtil.rnd(-8, 8), y = p.getY(), z = p.getZ() + MadUtil.rnd(-8, 8);
                        float pitch = (float) MadUtil.rnd(0.6, 1.6);
                        switch (MadUtil.rng().nextInt(10)) {
                            case 0 -> MadUtil.sound(w, x, y, z, SoundEvents.ENTITY_CHICKEN_AMBIENT, SoundCategory.AMBIENT, 1.2f, pitch);
                            case 1 -> MadUtil.sound(w, x, y, z, SoundEvents.ENTITY_PIG_AMBIENT, SoundCategory.AMBIENT, 1.2f, pitch);
                            case 2 -> MadUtil.sound(w, x, y, z, SoundEvents.ENTITY_COW_AMBIENT, SoundCategory.AMBIENT, 1.2f, pitch);
                            case 3 -> MadUtil.sound(w, x, y, z, SoundEvents.ENTITY_CREEPER_PRIMED, SoundCategory.AMBIENT, 1.0f, pitch);
                            case 4 -> MadUtil.sound(w, x, y, z, SoundEvents.ENTITY_VILLAGER_TRADE, SoundCategory.AMBIENT, 1.2f, pitch);
                            case 5 -> MadUtil.sound(w, x, y, z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 1.0f, pitch);
                            case 6 -> MadUtil.sound(w, x, y, z, SoundEvents.ENTITY_PARROT_IMITATE_CREEPER, SoundCategory.AMBIENT, 1.0f, pitch);
                            case 7 -> MadUtil.sound(w, x, y, z, SoundEvents.ENTITY_WITCH_CELEBRATE, SoundCategory.AMBIENT, 1.0f, pitch);
                            case 8 -> MadUtil.sound(w, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 0.3f, pitch + 0.4f);
                            default -> MadUtil.sound(w, x, y, z, SoundEvents.BLOCK_NOTE_BLOCK_BANJO, SoundCategory.AMBIENT, 1.5f, pitch);
                        }
                    }
                }).build());
        // 16
        reg(ChaosEvent.of("trampoline", "ПОЛ — ЭТО БАТУТ!", "Прыг-скок, прыг-скок").weight(7).duration(300).interval(10)
                .onStart(c -> c.players().forEach(p -> effect(p, StatusEffects.SLOW_FALLING, 400, 0)))
                .onTick((c, age) -> {
                    for (ServerPlayerEntity p : c.players()) {
                        if (p.isOnGround()) {
                            p.addVelocity(0, 0.85, 0);
                            p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(p));
                            MadUtil.sound(p, SoundEvents.ENTITY_SLIME_JUMP, 0.8f, (float) MadUtil.rnd(0.8, 1.3));
                        }
                    }
                }).build());
        // 17
        reg(ChaosEvent.of("darkness", "ЗАТМЕНИЕ ЗДРАВОГО СМЫСЛА!", "Свет ненадолго выключили").weight(3).minMadness(10).duration(200).interval(20)
                .onStart(c -> c.players().forEach(p -> effect(p, StatusEffects.DARKNESS, 160, 0)))
                .onTick((c, age) -> c.players().forEach(p -> MadUtil.sound(p, SoundEvents.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f))).build());
        // 18
        reg(ChaosEvent.of("gold_rush", "ЗОЛОТАЯ ЛИХОРАДКА!", "С неба посыпалось золото").weight(4).minMadness(20).duration(160).interval(4)
                .onTick((c, age) -> {
                    for (ServerPlayerEntity p : c.players()) {
                        ServerWorld w = MadUtil.world(p);
                        Item it = MadUtil.rng().nextInt(20) == 0 ? Items.GOLD_INGOT : Items.GOLD_NUGGET;
                        ItemEntity e = new ItemEntity(w, p.getX() + MadUtil.rnd(-5, 5), p.getY() + 14, p.getZ() + MadUtil.rnd(-5, 5), new ItemStack(it, 1 + MadUtil.rng().nextInt(3)));
                        e.setPickupDelay(10);
                        w.spawnEntity(e);
                        MobTracker.track(e, 600);
                    }
                }).build());
        // 19
        reg(ChaosEvent.of("levitation", "ВСЕ ВЗЛЕТЕЛИ!", "Только не смотрите вниз").weight(6).duration(60)
                .onStart(c -> {
                    for (ServerPlayerEntity p : c.players()) {
                        effect(p, StatusEffects.LEVITATION, 40, 1);
                        effect(p, StatusEffects.SLOW_FALLING, 240, 0);
                        int n = 0;
                        for (MobEntity m : MadUtil.world(p).getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(16), Entity::isAlive)) {
                            if (n++ >= 16) break;
                            effect(m, StatusEffects.LEVITATION, 40, 1);
                            effect(m, StatusEffects.SLOW_FALLING, 240, 0);
                        }
                    }
                }).build());
        // 20
        reg(ChaosEvent.of("royal_decree", "КОРОЛЕВСКИЙ УКАЗ!", "Земля дрожит. Кто-то идёт...").weight(3).minMadness(0).duration(260).interval(20)
                .condition(BossSummon::canSummon)
                .onStart(c -> {
                    ServerPlayerEntity p = c.randomPlayer();
                    if (p != null) BossSummon.begin(p);
                }).build());
        // 21
        reg(ChaosEvent.of("madman_visit", "ДЕРЕВЕНСКИЙ СУМАСШЕДШИЙ ПРИШЁЛ!", "У него выгоднейшие предложения").weight(6).minMadness(10).duration(40)
                .onStart(c -> spawnAround(c, ModEntities.VILLAGE_MADMAN, 1, 6000, 10, 18, null)).build());
        // 22
        reg(ChaosEvent.of("ninja_raid", "НАБЕГ КУР-НИНДЗЯ!", "Держите свои вещи крепче").weight(5).minMadness(20).duration(40)
                .onStart(c -> spawnAround(c, ModEntities.NINJA_CHICKEN, 2, 3600, 10, 18, null)).build());
        // 23
        reg(ChaosEvent.of("elite_skeletons", "ЭЛИТНЫЙ ОТРЯД СКЕЛЕТОВ!", "Стреляют метко. Почти всегда").weight(4).minMadness(35).duration(40).condition(ChaosEvents::notPeaceful)
                .onStart(c -> spawnAround(c, ModEntities.ELITE_SKELETON, 1, 3600, 14, 22, null)).build());
        // 24
        reg(ChaosEvent.of("teleport_sneeze", "ТЕЛЕПОРТАЦИОННЫЙ ЧИХ!", "Апчхи! Вы теперь не здесь").weight(5).minMadness(10).duration(40)
                .onStart(c -> {
                    for (ServerPlayerEntity p : c.players()) {
                        ServerWorld w = MadUtil.world(p);
                        BlockPos t = MadUtil.randomRing(w, p.getBlockPos(), 3, 6);
                        if (t == null) continue;
                        w.spawnParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.8, 0.4, 0.2);
                        p.requestTeleport(t.getX() + 0.5, t.getY(), t.getZ() + 0.5);
                        MadUtil.sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
                    }
                }).build());
        // 25
        reg(ChaosEvent.of("banana_storm", "БАНАНОВЫЙ ШТОРМ!", "Мобы подпрыгивают от радости").weight(6).minMadness(5).duration(160).interval(10)
                .onTick((c, age) -> {
                    for (ServerPlayerEntity p : c.players()) {
                        ServerWorld w = MadUtil.world(p);
                        int n = 0;
                        for (MobEntity m : w.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(14), Entity::isAlive)) {
                            if (n++ >= 10) break;
                            if (m instanceof PigEmperorEntity) continue;
                            w.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.YELLOW_DYE)), m.getX(), m.getY() + 1, m.getZ(), 5, 0.3, 0.3, 0.3, 0.05);
                            if (m.isOnGround() && MadUtil.rng().nextInt(3) == 0) m.addVelocity(MadUtil.rnd(-0.2, 0.2), 0.5, MadUtil.rnd(-0.2, 0.2));
                        }
                    }
                }).build());
    }
}
