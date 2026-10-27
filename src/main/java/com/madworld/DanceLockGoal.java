package com.madworld.entity;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

import java.util.EnumSet;

/** Пока моб танцует, блокирует движение и взгляд (иначе остальные цели его утащат). */
public class DanceLockGoal<T extends MobEntity & MadAnimated> extends Goal {
    private final T mob;

    public DanceLockGoal(T mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        return mob.getDanceTicks() > 0;
    }

    @Override
    public boolean shouldContinue() {
        return mob.getDanceTicks() > 0;
    }

    @Override
    public void start() {
        mob.getNavigation().stop();
    }
}
