package com.madworld.entity;

import com.madworld.item.ModItems;
import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.Supplier;

/**
 * Деревенский сумасшедший: предлагает нелепые сделки.
 * ПКМ — узнать предложение; ПКМ с нужным предметом в основной руке — сделка. Не больше 3 сделок, потом он «разоряется».
 */
public class VillageMadmanEntity extends MadMobEntity {
    private record Offer(Item wants, int wantCount, Supplier<ItemStack> gives, String line) {}

    private static ItemStack named(Item item, int count, String name) {
        ItemStack s = new ItemStack(item, count);
        s.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
        return s;
    }

    private static final Offer[] OFFERS = {
            new Offer(Items.DIAMOND, 1, () -> new ItemStack(Items.STICK, 64), "Дам 64 палки за один алмаз! Палки отборные, дубовые!"),
            new Offer(Items.DIRT, 1, () -> new ItemStack(Items.EMERALD, 2), "Обменяю обычную землю на изумруды! Никто не понимает ценности земли!"),
            new Offer(Items.GOLD_INGOT, 2, () -> named(Items.DIRT, 1, "Земля с историей"), "Два слитка золота за кусочек легендарной земли!"),
            new Offer(Items.APPLE, 1, () -> new ItemStack(Items.DIAMOND), "Яблоко! Дам алмаз! Только жене не говори."),
            new Offer(Items.BREAD, 3, () -> new ItemStack(Items.GOLD_INGOT), "Три хлеба за слиток! Я просто люблю хлеб."),
            new Offer(Items.ROTTEN_FLESH, 5, () -> new ItemStack(Items.EMERALD), "Гнильё — моя страсть! Пять штук — изумруд!"),
            new Offer(Items.EMERALD, 1, () -> new ItemStack(Items.COBBLESTONE, 64), "Один изумруд — целая гора булыжника!"),
            new Offer(Items.FEATHER, 2, () -> new ItemStack(ModItems.LUCKY_DIE), "Два пера за кубик удачи! Перья нынче в цене."),
            new Offer(Items.POTATO, 1, () -> new ItemStack(ModItems.FORBIDDEN_POTATO), "Обменяю картошку на... другую картошку."),
            new Offer(Items.IRON_INGOT, 1, () -> new ItemStack(Items.SNOWBALL, 16), "Железо за снежки! Лето скоро, надо запасаться!"),
            new Offer(Items.DIAMOND, 1, () -> new ItemStack(ModItems.SPEED_SOUP, 3), "Алмаз за три супа! Супы — это будущее!"),
            new Offer(Items.WHEAT, 4, () -> new ItemStack(ModItems.BANANA_GRENADE, 4), "Пшеница на гранаты! Фермерам — вооружение!")
    };

    private static final String[] RAVING = {
            "Я видел курицу, которая знала мою фамилию!", "Не доверяйте блокам. Они всё слышат.", "Свинья в короне — это не шутка!",
            "А вы знали, что земля круглая? Нет, квадратная!", "Я продаю воздух. Дорого!", "Тссс... облака — это ваты. Я проверял.",
            "У меня есть друг-крипер. Он сказал, что не взорвётся.", "Картошка смотрит. Всегда смотрит."
    };

    private Offer offer = OFFERS[0];
    private int trades;
    private int offerAge;
    private int leaveTicks = -1;

    public VillageMadmanEntity(EntityType<? extends VillageMadmanEntity> type, World world) {
        super(type, world);
        this.offer = OFFERS[MadUtil.rng().nextInt(OFFERS.length)];
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.45)
                .add(EntityAttributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, danceLock());
        this.goalSelector.add(2, new EscapeDangerGoal(this, 1.6));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.1));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override
    protected void hurtSound() {
        MadUtil.sound(this, SoundEvents.ENTITY_VILLAGER_HURT, 1.0f, 0.8f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(getEntityWorld() instanceof ServerWorld sw)) return;
        if (++offerAge > 6000) {
            offerAge = 0;
            offer = OFFERS[MadUtil.rng().nextInt(OFFERS.length)];
        }
        if (age % 300 == 0 && leaveTicks < 0) {
            for (ServerPlayerEntity p : sw.getPlayers()) {
                if (p.isAlive() && !p.isSpectator() && squaredDistanceTo(p) < 64) {
                    p.sendMessage(Text.literal("§6[Сумасшедший] §f" + RAVING[MadUtil.rng().nextInt(RAVING.length)]), true);
                    break;
                }
            }
        }
        if (leaveTicks > 0 && --leaveTicks == 0) {
            sw.spawnParticles(ParticleTypes.POOF, getX(), getY() + 1, getZ(), 15, 0.3, 0.5, 0.3, 0.03);
            discard();
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (getEntityWorld().isClient()) return ActionResult.SUCCESS;
        if (!(getEntityWorld() instanceof ServerWorld sw)) return ActionResult.PASS;
        if (leaveTicks >= 0) {
            player.sendMessage(Text.literal("§6[Сумасшедший] §fУходи, я разорён!"), true);
            return ActionResult.SUCCESS;
        }
        ItemStack held = player.getStackInHand(hand);
        if (held.isOf(offer.wants()) && held.getCount() >= offer.wantCount()) {
            held.decrement(offer.wantCount());
            player.getInventory().offerOrDrop(offer.gives().get());
            trades++;
            MadUtil.sound(this, SoundEvents.ENTITY_VILLAGER_TRADE, 1.0f, 1.0f);
            sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 1.6, getZ(), 10, 0.4, 0.4, 0.4, 0.05);
            player.sendMessage(Text.literal("§6[Сумасшедший] §fСделка! Ха-ха! Ты меня разорил!"), false);
            if (sw.getServer() != null) MadnessManager.add(sw.getServer(), 1.0);
            if (trades >= 3) {
                player.sendMessage(Text.literal("§6[Сумасшедший] §fВсё, товар кончился. Побегу за новым."), false);
                leaveTicks = 100;
            } else {
                offer = OFFERS[MadUtil.rng().nextInt(OFFERS.length)];
            }
        } else {
            MadUtil.sound(this, SoundEvents.ENTITY_VILLAGER_AMBIENT, 1.0f, 0.6f);
            String what = offer.wantCount() + "× " + new ItemStack(offer.wants()).getName().getString();
            player.sendMessage(Text.literal("§6[Сумасшедший] §f" + offer.line() + " §7(нужно в руке: " + what + ")"), false);
        }
        return ActionResult.SUCCESS;
    }
}
