package com.madworld.entity;

/** Общий интерфейс «танцующих» мобов — читается клиентским рендерером. */
public interface MadAnimated {
    boolean isDancing();

    void startDance(int ticks);

    int getDanceTicks();

    /** Руки подняты/вытянуты вперёд (зомби, скелет-стрелок). */
    default boolean isArmsUp() {
        return false;
    }
}
