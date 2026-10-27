package com.madworld.item;

import com.madworld.entity.ModEntities;
import com.madworld.event.EventManager;
import com.madworld.madness.MadnessManager;
import com.madworld.util.Advancements;
import com.madworld.util.MadUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/** Запрещённая картошка: при поедании запускает случайный безумный эффект. Никогда не смертельна. */
public class ForbiddenPotatoItem extends Item {
    public ForbiddenPotatoItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);
        if (!(world instanceof ServerWorld sw) || !(user instanceof ServerPlayerEntity p)) return result;
        Advancements.grant(p, "potato_changed_life");
        String msg;
        switch (MadUtil.rng().nextInt(10)) {
            case 0 -> {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 400, 2));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
                msg = "Картошка ускорила мир (или вас).";
            }
            case 1 -> {
                p.addVelocity(0, 1.6, 0);
                p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(p));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 300, 0));
                msg = "Картошка-ракета! Пуск!";
            }
            case 2 -> {
                boolean ok = sw.getServer() != null && EventManager.triggerRandom(sw.getServer(), true);
                msg = ok ? "Картошка открыла портал в хаос!" : "Картошка пыталась открыть портал в хаос, но тот занят.";
            }
            case 3 -> {
                for (int i = 0; i < 6; i++) {
                    net.minecraft.entity.ItemEntity e = new net.minecraft.entity.ItemEntity(sw, p.getX() + MadUtil.rnd(-2, 2), p.getY() + 8, p.getZ() + MadUtil.rnd(-2, 2), new ItemStack(Items.BAKED_POTATO));
                    e.setPickupDelay(10);
                    sw.spawnEntity(e);
                }
                msg = "Картофельный дождь!";
            }
            case 4 -> {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 1200, 0));
                msg = "Вы — картошка. Вы светитесь.";
            }
            case 5 -> {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 400, 4));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 500, 0));
                msg = "Ноги превратились в пружины.";
            }
            case 6 -> {
                p.heal(p.getMaxHealth());
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 600, 1));
                msg = "Картошка исцелила вас. Ничего не спрашивайте.";
            }
            case 7 -> {
                for (MobEntity m : sw.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(12), e -> e.isAlive())) {
                    m.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 100, 0));
                    m.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 50, 0));
                    m.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 300, 0));
                }
                msg = "Картофельная левитация для всех вокруг!";
            }
            case 8 -> {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 600, 0));
                msg = "Вас не видно. Вы ведь просто картошка.";
            }
            default -> {
                p.getInventory().offerOrDrop(new ItemStack(ModItems.GOLDEN_POTATO, 2));
                p.getInventory().offerOrDrop(new ItemStack(ModItems.BANANA_GRENADE, 2));
                msg = "Из картошки выпал подарок. Картошка щедра.";
            }
        }
        MadUtil.sound(p, SoundEvents.ENTITY_WITCH_CELEBRATE, 1.0f, 1.0f);
        p.sendMessage(Text.literal("§5[Запрещённая картошка] §f" + msg), false);
        if (sw.getServer() != null) MadnessManager.add(sw.getServer(), 3.0);
        return result;
    }
}
