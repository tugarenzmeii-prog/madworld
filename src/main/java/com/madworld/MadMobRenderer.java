package com.madworld.client;

import com.madworld.MadWorldMod;
import com.madworld.entity.MadAnimated;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class MadMobRenderer<T extends MobEntity & MadAnimated> extends MobEntityRenderer<T, MadRenderState, MadMobModel> {
    private final Identifier texture;

    public MadMobRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, MadMobModel.Kind kind, String textureName, float shadowRadius) {
        super(ctx, new MadMobModel(ctx.getPart(layer), kind), shadowRadius);
        this.texture = MadWorldMod.id("textures/entity/" + textureName + ".png");
    }

    @Override
    public MadRenderState createRenderState() {
        return new MadRenderState();
    }

    @Override
    public void updateRenderState(T entity, MadRenderState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        state.dancing = entity.isDancing();
        state.armsUp = entity.isArmsUp();
    }

    @Override
    public Identifier getTexture(MadRenderState state) {
        return texture;
    }
}
