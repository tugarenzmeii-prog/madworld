package com.madworld;

import com.madworld.event.EventManager;
import com.madworld.event.MadSpawner;
import com.madworld.item.HammerOfFate;
import com.madworld.item.ModItems;
import com.madworld.madness.MadnessManager;
import com.madworld.util.Advancements;
import com.madworld.util.MadUtil;
import com.madworld.util.MobTracker;
import com.madworld.util.Tasks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/** Все подписки на события сервера. Каждая защищена от исключений, чтобы мод не ронял игру. */
public final class MadHooks {
    private MadHooks() {}

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Tasks.clear();
            MobTracker.clear();
            EventManager.reset();
            MadnessManager.load(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            MadnessManager.save();
            Tasks.clear();
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                Tasks.tick();
                EventManager.tick(server);
                MobTracker.tick(server);
                MadSpawner.tick(server);
                MadnessManager.tick(server);
            } catch (Throwable t) {
                MadWorldMod.LOGGER.error("Ошибка в тике MadWorld", t);
            }
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            try {
                MobTracker.onLoad(entity);
            } catch (Throwable t) {
                MadWorldMod.LOGGER.warn("Ошибка при загрузке сущности", t);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                Advancements.grant(handler.getPlayer(), "root"));

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, be) -> {
            if (state.isIn(BlockTags.LOGS) && player instanceof ServerPlayerEntity sp) EventManager.noteLogBroken(sp);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (player.getMainHandStack().isOf(ModItems.HAMMER_OF_FATE) && world instanceof ServerWorld sw) {
                try {
                    HammerOfFate.onAttack(player, sw, entity);
                } catch (Throwable t) {
                    MadWorldMod.LOGGER.warn("Ошибка Молотка судьбы", t);
                }
            }
            return ActionResult.PASS;
        });

        // Золотой унитаз изредка попадается в сундуках со структурами
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && key.getValue().getPath().startsWith("chests/")) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.03f))
                        .with(ItemEntry.builder(ModItems.GOLDEN_TOILET))
                        .build());
            }
        });
    }
}
