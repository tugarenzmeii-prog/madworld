package com.madworld;

import com.madworld.entity.ModEntities;
import com.madworld.event.BossSummon;
import com.madworld.event.ChaosEvent;
import com.madworld.event.ChaosEvents;
import com.madworld.event.EventManager;
import com.madworld.madness.MadnessManager;
import com.madworld.util.MadUtil;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /madworld status | event <id> | madness <число> | boss | spawn <моб>
 * Доступно из консоли сервера и в одиночной игре (на многопользовательском сервере игроки команду не используют).
 */
public final class MadCommands {
    private static final List<String> MOBS = List.of("creeper_courier", "zombie_dancer", "village_madman", "ninja_chicken", "elite_skeleton", "pig_emperor");

    private MadCommands() {}

    private static boolean allowed(ServerCommandSource s) {
        return s.getEntity() == null || s.getServer().isSingleplayer();
    }

    private static int deny(CommandContext<ServerCommandSource> c) {
        c.getSource().sendError(Text.literal("Команда доступна только из консоли или в одиночной игре."));
        return 0;
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal("madworld")
                        .then(CommandManager.literal("status").executes(c -> {
                            if (!allowed(c.getSource())) return deny(c);
                            String msg = String.format("Безумие: %.1f / %d, пережито событий: %d, активных мобов-extra лимит: %d",
                                    MadnessManager.get(), com.madworld.config.MadConfig.get().maxMadness,
                                    MadnessManager.eventsSurvived(), com.madworld.config.MadConfig.get().maxExtraMobs);
                            c.getSource().sendFeedback(() -> Text.literal(msg), false);
                            return 1;
                        }))
                        .then(CommandManager.literal("event").then(CommandManager.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> CommandSource.suggestMatching(ChaosEvents.ALL.stream().map(e -> e.id).collect(Collectors.toList()), b))
                                .executes(c -> {
                                    if (!allowed(c.getSource())) return deny(c);
                                    ChaosEvent ev = EventManager.byId(StringArgumentType.getString(c, "id"));
                                    if (ev == null) {
                                        c.getSource().sendError(Text.literal("Нет такого события."));
                                        return 0;
                                    }
                                    boolean ok = EventManager.start(c.getSource().getServer(), ev, true);
                                    if (!ok) c.getSource().sendError(Text.literal("Событие не удалось запустить (уже активно или нет игроков)."));
                                    return ok ? 1 : 0;
                                })))
                        .then(CommandManager.literal("madness").then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0, 1000))
                                .executes(c -> {
                                    if (!allowed(c.getSource())) return deny(c);
                                    MadnessManager.set(c.getSource().getServer(), DoubleArgumentType.getDouble(c, "value"));
                                    c.getSource().sendFeedback(() -> Text.literal("Безумие установлено: " + MadnessManager.get()), false);
                                    return 1;
                                })))
                        .then(CommandManager.literal("boss").executes(c -> {
                            if (!allowed(c.getSource())) return deny(c);
                            ServerPlayerEntity p = c.getSource().getPlayer();
                            if (p == null) {
                                c.getSource().sendError(Text.literal("Нужен игрок."));
                                return 0;
                            }
                            return BossSummon.ritual(p) ? 1 : 0;
                        }))
                        .then(CommandManager.literal("spawn").then(CommandManager.argument("mob", StringArgumentType.word())
                                .suggests((c, b) -> CommandSource.suggestMatching(MOBS, b))
                                .executes(c -> {
                                    if (!allowed(c.getSource())) return deny(c);
                                    ServerPlayerEntity p = c.getSource().getPlayer();
                                    if (p == null) {
                                        c.getSource().sendError(Text.literal("Нужен игрок."));
                                        return 0;
                                    }
                                    EntityType<? extends Entity> type = switch (StringArgumentType.getString(c, "mob")) {
                                        case "creeper_courier" -> ModEntities.CREEPER_COURIER;
                                        case "zombie_dancer" -> ModEntities.ZOMBIE_DANCER;
                                        case "village_madman" -> ModEntities.VILLAGE_MADMAN;
                                        case "ninja_chicken" -> ModEntities.NINJA_CHICKEN;
                                        case "elite_skeleton" -> ModEntities.ELITE_SKELETON;
                                        case "pig_emperor" -> ModEntities.PIG_EMPEROR;
                                        default -> null;
                                    };
                                    if (type == null) {
                                        c.getSource().sendError(Text.literal("Неизвестный моб."));
                                        return 0;
                                    }
                                    return type.spawn(MadUtil.world(p), p.getBlockPos().add(2, 0, 2), SpawnReason.COMMAND) != null ? 1 : 0;
                                })))));
    }
}
