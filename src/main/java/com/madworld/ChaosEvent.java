package com.madworld.event;

import net.minecraft.server.MinecraftServer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Описание одного безумного события. minMadnessPercent — минимальный уровень безумия (в % от максимума). */
public final class ChaosEvent {
    public final String id, title, subtitle;
    public final int weight, minMadnessPercent, duration, interval;
    final Consumer<EventContext> onStart;
    final BiConsumer<EventContext, Integer> onTick;
    final Consumer<EventContext> onEnd;
    final Predicate<MinecraftServer> condition;

    private ChaosEvent(Builder b) {
        this.id = b.id;
        this.title = b.title;
        this.subtitle = b.subtitle;
        this.weight = b.weight;
        this.minMadnessPercent = b.minMadness;
        this.duration = Math.max(20, b.duration);
        this.interval = Math.max(1, b.interval);
        this.onStart = b.onStart;
        this.onTick = b.onTick;
        this.onEnd = b.onEnd;
        this.condition = b.condition;
    }

    public static Builder of(String id, String title, String subtitle) {
        return new Builder(id, title, subtitle);
    }

    public static final class Builder {
        private final String id, title, subtitle;
        private int weight = 6, minMadness = 0, duration = 40, interval = 20;
        private Consumer<EventContext> onStart = c -> {};
        private BiConsumer<EventContext, Integer> onTick;
        private Consumer<EventContext> onEnd = c -> {};
        private Predicate<MinecraftServer> condition = s -> true;

        private Builder(String id, String title, String subtitle) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
        }

        public Builder weight(int w) { this.weight = w; return this; }
        public Builder minMadness(int percent) { this.minMadness = percent; return this; }
        public Builder duration(int ticks) { this.duration = ticks; return this; }
        public Builder interval(int ticks) { this.interval = ticks; return this; }
        public Builder onStart(Consumer<EventContext> c) { this.onStart = c; return this; }
        public Builder onTick(BiConsumer<EventContext, Integer> c) { this.onTick = c; return this; }
        public Builder onEnd(Consumer<EventContext> c) { this.onEnd = c; return this; }
        public Builder condition(Predicate<MinecraftServer> p) { this.condition = p; return this; }
        public ChaosEvent build() { return new ChaosEvent(this); }
    }
}
