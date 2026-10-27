package com.madworld.item;

import com.madworld.entity.ModEntities;
import com.madworld.event.EventManager;
import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Кубик удачи: бросок d100 — от джекпота до безобидной неприятности. Никогда не убивает. */
public class LuckyDieItem extends Item {
    public LuckyDieItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld sw) || !(user instanceof ServerPlayerEntity p)) return ActionResult.SUCCESS;
        ItemStack stack = user.getStackInHand(hand);
        user.getItemCooldownManager().set(stack, 20);
        stack.decrementUnlessCreative(1, user);
        int r = MadUtil.rng().nextInt(100);
        String msg;
        if (r < 10) { give(p, new ItemStack(Items.DIAMOND, 2)); msg = "Выпало 6! Два алмаза!"; }
        else if (r < 20) { give(p, new ItemStack(Items.IRON_INGOT, 8)); msg = "Железный день: 8 слитков."; }
        else if (r < 28) { give(p, new ItemStack(Items.GOLDEN_APPLE)); msg = "Золотое яблоко. Приятного!"; }
        else if (r < 36) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600, 1));
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 600, 1));
            msg = "Ноги сами несут: скорость и прыгучесть!";
        } else if (r < 44) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 1));
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 300, 0));
            msg = "Взлёт! (Падать будете мягко.)";
        } else if (r < 52) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0));
            msg = "Ой, свет выключили!";
        } else if (r < 60) {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 160, 0));
            msg = "Мир немного поплыл...";
        } else if (r < 68) {
            for (int i = 0; i < 3; i++) {
                BlockPos pos = MadUtil.randomRing(sw, p.getBlockPos(), 2, 5);
                if (pos != null) MadUtil.spawnExtra(sw, EntityType.CHICKEN, pos, 1200);
            }
            msg = "Курицы! Откуда?!";
        } else if (r < 76) {
            boolean ok = sw.getServer() != null && EventManager.triggerRandom(sw.getServer(), true);
            msg = ok ? "Кубик запустил событие!" : "Кубик попытался запустить событие, но хаос занят.";
        } else if (r < 84) { give(p, new ItemStack(Items.DIRT)); msg = "Ничего. Совсем. Только земля."; }
        else if (r < 90) {
            MadUtil.sound(p, SoundEvents.ENTITY_CREEPER_PRIMED, 1.5f, 1.0f);
            sw.spawnParticles(ParticleTypes.SMOKE, p.getX(), p.getY() + 1.0, p.getZ(), 20, 0.5, 0.5, 0.5, 0.02);
            msg = "Шшш... Шутка! Вы же не думали, что это серьёзно?";
        } else if (r < 95) {
            give(p, new ItemStack(ModItems.BANANA_GRENADE, 3));
            give(p, new ItemStack(ModItems.SPEED_SOUP));
            msg = "Бананы и суп. Странный набор, но ладно.";
        } else if (r < 99) { give(p, new ItemStack(ModItems.GOLDEN_POTATO, 3)); msg = "Золотая картошка! Три штуки."; }
        else { give(p, new ItemStack(ModItems.GOLDEN_TOILET)); msg = "ДЖЕКПОТ! Золотой унитаз!"; }
        p.sendMessage(Text.literal("§e[Кубик удачи] §f" + msg), false);
        if (sw.getServer() != null) MadnessManager.add(sw.getServer(), 1.0);
        return ActionResult.SUCCESS;
    }

    private static void give(ServerPlayerEntity p, ItemStack s) {
        p.getInventory().offerOrDrop(s);
    }
}
