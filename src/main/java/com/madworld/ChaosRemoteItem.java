package com.madworld.item;

import com.madworld.event.BossSummon;
import com.madworld.event.EventManager;
import com.madworld.madness.MadnessManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/** Пульт хаоса: ПКМ — случайное событие; Shift+ПКМ — обряд вызова Свиного Императора (если мир достаточно безумен). */
public class ChaosRemoteItem extends Item {
    public ChaosRemoteItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!(world instanceof ServerWorld sw) || !(user instanceof ServerPlayerEntity sp)) return ActionResult.SUCCESS;
        ItemStack stack = user.getStackInHand(hand);
        MinecraftServer server = sw.getServer();
        if (server == null) return ActionResult.SUCCESS;
        if (user.isSneaking()) {
            if (BossSummon.ritual(sp)) user.getItemCooldownManager().set(stack, 1200);
            return ActionResult.SUCCESS;
        }
        if (EventManager.triggerRandom(server, true)) {
            user.getItemCooldownManager().set(stack, 400);
            MadnessManager.add(server, 1.5);
        } else {
            sp.sendMessage(Text.literal("Хаос сейчас занят. Попробуйте позже."), true);
        }
        return ActionResult.SUCCESS;
    }
}
