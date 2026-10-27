package com.madworld.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Суп скорости: Скорость III на 45 секунд, Прыгучесть на 15, пара секунд голода в нагрузку. */
public class SpeedSoupItem extends Item {
    public SpeedSoupItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack result = super.finishUsing(stack, world, user);
        if (!world.isClient()) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 45 * 20, 2));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 15 * 20, 1));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 5 * 20, 0));
        }
        return result;
    }
}
