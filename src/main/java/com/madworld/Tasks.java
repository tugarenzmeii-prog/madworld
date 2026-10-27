package com.madworld.util;

import com.madworld.MadWorldMod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntConsumer;

/** Лёгкий планировщик на серверных тиках. Ошибка в задаче не роняет сервер, число задач ограничено. */
public final class Tasks {
    private static final int MAX_TASKS = 256;

    private static final class Task {
        int age;
        final int duration, interval;
        final IntConsumer body;
        final Runnable end;

        Task(int duration, int interval, IntConsumer body, Runnable end) {
            this.duration = Math.max(1, duration);
            this.interval = Math.max(1, interval);
            this.body = body;
            this.end = end;
        }
    }

    private static final List<Task> LIST = new ArrayList<>();
    private static final List<Task> PENDING = new ArrayList<>();

    private Tasks() {}

    /** Выполняет body каждые interval тиков в течение duration тиков, затем end. */
    public static void repeat(int duration, int interval, IntConsumer body, Runnable end) {
        if (LIST.size() + PENDING.size() >= MAX_TASKS) {
            MadWorldMod.LOGGER.warn("Слишком много активных задач MadWorld, задача пропущена");
            if (end != null) safe(end);
            return;
        }
        PENDING.add(new Task(duration, interval, body, end));
    }

    public static void later(int delay, Runnable r) {
        repeat(delay, delay, null, r);
    }

    public static void tick() {
        if (!PENDING.isEmpty()) {
            LIST.addAll(PENDING);
            PENDING.clear();
        }
        Iterator<Task> it = LIST.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            t.age++;
            try {
                if (t.body != null && t.age % t.interval == 0) t.body.accept(t.age);
                if (t.age >= t.duration) {
                    it.remove();
                    if (t.end != null) t.end.run();
                }
            } catch (Throwable ex) {
                MadWorldMod.LOGGER.error("Ошибка в задаче MadWorld", ex);
                it.remove();
            }
        }
    }

    public static void clear() {
        LIST.clear();
        PENDING.clear();
    }

    private static void safe(Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            MadWorldMod.LOGGER.error("Ошибка в задаче MadWorld", t);
        }
    }
}
