package com.madworld.madness;

import com.madworld.MadWorldMod;
import com.madworld.config.MadConfig;
import com.madworld.util.Advancements;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Скрытый показатель безумия мира. Хранится в <мир>/madworld_state.txt. */
public final class MadnessManager {
    private static double madness;
    private static int eventsSurvived;
    private static Path file;
    private static int ticks;
    private static boolean dirty;
    private static boolean fiftyGranted;

    private MadnessManager() {}

    public static void load(MinecraftServer server) {
        madness = MadConfig.get().startMadness;
        eventsSurvived = 0;
        fiftyGranted = false;
        try {
            file = server.getSavePath(WorldSavePath.ROOT).resolve("madworld_state.txt");
            if (Files.exists(file)) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (String l : lines) {
                    String[] kv = l.split("=", 2);
                    if (kv.length != 2) continue;
                    if (kv[0].trim().equals("madness")) madness = Double.parseDouble(kv[1].trim());
                    if (kv[0].trim().equals("events")) eventsSurvived = Integer.parseInt(kv[1].trim());
                    if (kv[0].trim().equals("fifty")) fiftyGranted = Boolean.parseBoolean(kv[1].trim());
                }
            }
        } catch (Exception e) {
            MadWorldMod.LOGGER.warn("Не удалось прочитать состояние безумия, начинаем заново", e);
        }
        madness = clamp(madness);
    }

    public static void save() {
        if (file == null) return;
        try {
            Files.writeString(file, "madness=" + madness + "\nevents=" + eventsSurvived + "\nfifty=" + fiftyGranted + "\n", StandardCharsets.UTF_8);
            dirty = false;
        } catch (IOException e) {
            MadWorldMod.LOGGER.warn("Не удалось сохранить состояние безумия", e);
        }
    }

    public static double get() {
        return madness;
    }

    public static double fraction() {
        return madness / Math.max(1, MadConfig.get().maxMadness);
    }

    public static int eventsSurvived() {
        return eventsSurvived;
    }

    public static void set(MinecraftServer server, double value) {
        madness = clamp(value);
        dirty = true;
        checkMilestones(server);
    }

    public static void add(MinecraftServer server, double amount) {
        MadConfig c = MadConfig.get();
        if (!c.madnessEnabled || server == null) return;
        madness = clamp(madness + amount * c.madnessGainMultiplier);
        dirty = true;
        checkMilestones(server);
    }

    public static void eventSurvived(MinecraftServer server) {
        eventsSurvived++;
        dirty = true;
        if (eventsSurvived == 10) {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) Advancements.grant(p, "survived_chaos");
        }
    }

    private static void checkMilestones(MinecraftServer server) {
        if (!fiftyGranted && fraction() >= 0.5) {
            fiftyGranted = true;
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) Advancements.grant(p, "is_this_minecraft");
        }
    }

    public static void tick(MinecraftServer server) {
        ticks++;
        if (ticks % 3000 == 0 && madness > 0 && MadConfig.get().madnessEnabled) {
            madness = clamp(madness - 1); // безумие слегка остывает
            dirty = true;
        }
        if (ticks % 1200 == 0 && dirty) save();
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(MadConfig.get().maxMadness, v));
    }
}
