package com.madworld.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;

/**
 * Универсальная модель для всех мобов мода. Части ищутся по имени:
 * head, body, leg0..leg3 (QUAD), arm_r/arm_l/leg_r/leg_l (BIPED), wing_r/wing_l/leg_r/leg_l (BIRD).
 */
public class MadMobModel extends EntityModel<MadRenderState> {
    public enum Kind { QUAD, BIPED, BIRD }

    private static final float DEG = (float) Math.PI / 180f;

    private final Kind kind;
    private final ModelPart head, body;
    private final ModelPart[] legs = new ModelPart[4];
    private final ModelPart armR, armL, legR, legL, wingR, wingL;

    public MadMobModel(ModelPart root, Kind kind) {
        super(root);
        this.kind = kind;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        for (int i = 0; i < 4; i++) legs[i] = root.hasChild("leg" + i) ? root.getChild("leg" + i) : null;
        this.armR = root.hasChild("arm_r") ? root.getChild("arm_r") : null;
        this.armL = root.hasChild("arm_l") ? root.getChild("arm_l") : null;
        this.legR = root.hasChild("leg_r") ? root.getChild("leg_r") : null;
        this.legL = root.hasChild("leg_l") ? root.getChild("leg_l") : null;
        this.wingR = root.hasChild("wing_r") ? root.getChild("wing_r") : null;
        this.wingL = root.hasChild("wing_l") ? root.getChild("wing_l") : null;
    }

    @Override
    public void setAngles(MadRenderState s) {
        super.setAngles(s);
        float swing = s.limbSwingAnimationProgress * 0.6662f;
        float amp = s.limbSwingAmplitude;
        head.yaw = s.relativeHeadYaw * DEG;
        head.pitch = s.pitch * DEG;

        switch (kind) {
            case QUAD -> {
                float a = (float) Math.cos(swing) * 1.4f * amp, b = (float) Math.cos(swing + Math.PI) * 1.4f * amp;
                if (legs[0] != null) legs[0].pitch = a;
                if (legs[1] != null) legs[1].pitch = b;
                if (legs[2] != null) legs[2].pitch = b;
                if (legs[3] != null) legs[3].pitch = a;
            }
            case BIPED -> {
                if (legR != null) legR.pitch = (float) Math.cos(swing) * 1.4f * amp;
                if (legL != null) legL.pitch = (float) Math.cos(swing + Math.PI) * 1.4f * amp;
                float armSwing = s.armsUp ? 0f : 1.0f;
                if (armR != null) armR.pitch = s.armsUp ? -1.45f : (float) Math.cos(swing + Math.PI) * 2.0f * amp * 0.5f * armSwing;
                if (armL != null) armL.pitch = s.armsUp ? -1.45f : (float) Math.cos(swing) * 2.0f * amp * 0.5f * armSwing;
            }
            case BIRD -> {
                if (legR != null) legR.pitch = (float) Math.cos(swing) * 1.4f * amp;
                if (legL != null) legL.pitch = (float) Math.cos(swing + Math.PI) * 1.4f * amp;
                float flap = amp > 0.1f ? (float) Math.sin(s.age * 1.4f) * 0.7f + 0.9f : 0.1f;
                if (wingR != null) wingR.roll = flap;
                if (wingL != null) wingL.roll = -flap;
            }
        }

        if (s.dancing) {
            float t = s.age * 0.8f;
            head.roll = (float) Math.sin(t) * 0.45f;
            body.roll = (float) Math.sin(t) * 0.18f;
            if (armR != null) { armR.pitch = -2.7f + (float) Math.sin(t) * 0.5f; armR.roll = 0.5f; }
            if (armL != null) { armL.pitch = -2.7f + (float) Math.cos(t) * 0.5f; armL.roll = -0.5f; }
            if (wingR != null) wingR.roll = 1.2f + (float) Math.sin(t * 2) * 0.5f;
            if (wingL != null) wingL.roll = -1.2f - (float) Math.sin(t * 2) * 0.5f;
            float a = (float) Math.sin(t) * 0.9f;
            if (legR != null) legR.pitch = a;
            if (legL != null) legL.pitch = -a;
            for (int i = 0; i < 4; i++) if (legs[i] != null) legs[i].pitch = (i % 2 == 0 ? a : -a);
        }
    }
}
