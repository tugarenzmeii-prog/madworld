package com.madworld.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.madworld.MadWorldMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Файл: config/madworld.json. Все значения проверяются и ограничиваются при загрузке. */
public final class MadConfig {
    /** Включить случайные события. */
    public boolean eventsEnabled = true;
    /** Минимальный интервал между событиями (секунды). */
    public int minIntervalSeconds = 120;
    /** Максимальный интервал между событиями (секунды). */
    public int maxIntervalSeconds = 360;
    /** Максимум дополнительных мобов/существ мода в одном измерении одновременно. */
    public int maxExtraMobs = 40;
    /** Включить систему безумия (если false — уровень всегда равен startMadness). */
    public boolean madnessEnabled = true;
    /** Стартовый уровень безумия нового мира (0..maxMadness). */
    public int startMadness = 0;
    /** Максимальный уровень безумия (10..1000). */
    public int maxMadness = 100;
    /** Множитель прироста безумия (0..10). */
    public double madnessGainMultiplier = 1.0;
    /** Уровень безумия (в процентах от максимума), с которого возможен вызов Свиного Императора. */
    public int bossMadnessPercent = 75;
    /** Появляются ли новые мобы сами по мере роста безумия. */
    public boolean naturalMadMobs = true;
    /** Максимальная шанс (0..1) появления моба за проверку при 100% безумия. */
    public double naturalSpawnChanceAtMax = 0.35;
    /** Список id отключённых событий. */
    public List<String> disabledEvents = new ArrayList<>();

    private static MadConfig instance = new MadConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static MadConfig get() {
        return instance;
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("madworld.json");
        MadConfig loaded = null;
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                loaded = GSON.fromJson(r, MadConfig.class);
            } catch (Exception e) {
                MadWorldMod.LOGGER.error("Не удалось прочитать madworld.json, используются значения по умолчанию", e);
            }
        }
        if (loaded == null) loaded = new MadConfig();
        loaded.sanitize();
        instance = loaded;
        try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(loaded, w);
        } catch (IOException e) {
            MadWorldMod.LOGGER.warn("Не удалось сохранить madworld.json", e);
        }
    }

    private void sanitize() {
        minIntervalSeconds = clamp(minIntervalSeconds, 10, 36000);
        maxIntervalSeconds = clamp(maxIntervalSeconds, minIntervalSeconds, 72000);
        maxExtraMobs = clamp(maxExtraMobs, 0, 200);
        maxMadness = clamp(maxMadness, 10, 1000);
        startMadness = clamp(startMadness, 0, maxMadness);
        bossMadnessPercent = clamp(bossMadnessPercent, 10, 100);
        madnessGainMultiplier = Math.max(0.0, Math.min(10.0, madnessGainMultiplier));
        naturalSpawnChanceAtMax = Math.max(0.0, Math.min(1.0, naturalSpawnChanceAtMax));
        if (disabledEvents == null) disabledEvents = new ArrayList<>();
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
