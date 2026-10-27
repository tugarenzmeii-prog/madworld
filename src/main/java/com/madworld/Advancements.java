package com.madworld.util;

import com.madworld.MadWorldMod;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Advancements {
    private Advancements() {}

    /** Выдаёт достижение мода (критерий "trigger"). Безопасно вызывать повторно. */
    public static void grant(ServerPlayerEntity player, String name) {
        try {
            MinecraftServer server = player.getEntityWorld().getServer();
            if (server == null) return;
            AdvancementEntry entry = server.getAdvancementLoader().get(MadWorldMod.id(name));
            if (entry != null) player.getAdvancementTracker().grantCriterion(entry, "trigger");
        } catch (Throwable t) {
            MadWorldMod.LOGGER.warn("Не удалось выдать достижение {}", name, t);
        }
    }
}
