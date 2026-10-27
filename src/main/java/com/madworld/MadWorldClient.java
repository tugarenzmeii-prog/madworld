package com.madworld.client;

import com.madworld.entity.ModEntities;
import com.madworld.net.ModNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class MadWorldClient implements ClientModInitializer {
    /** Сколько клиентских тиков ещё действует «перевёрнутое управление». */
    private static int invertedTicks;
    private static boolean swapped;
    private static boolean wasF, wasB, wasL, wasR;

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(MadModels.CREEPER_COURIER, MadModels::creeper_courier);
        EntityModelLayerRegistry.registerModelLayer(MadModels.PIG_EMPEROR, MadModels::pig_emperor);
        EntityModelLayerRegistry.registerModelLayer(MadModels.ZOMBIE_DANCER, MadModels::zombie_dancer);
        EntityModelLayerRegistry.registerModelLayer(MadModels.VILLAGE_MADMAN, MadModels::village_madman);
        EntityModelLayerRegistry.registerModelLayer(MadModels.NINJA_CHICKEN, MadModels::ninja_chicken);
        EntityModelLayerRegistry.registerModelLayer(MadModels.ELITE_SKELETON, MadModels::elite_skeleton);

        EntityRendererRegistry.register(ModEntities.CREEPER_COURIER, ctx -> new MadMobRenderer<>(ctx, MadModels.CREEPER_COURIER, MadMobModel.Kind.QUAD, "creeper_courier", 0.5f));
        EntityRendererRegistry.register(ModEntities.PIG_EMPEROR, ctx -> new MadMobRenderer<>(ctx, MadModels.PIG_EMPEROR, MadMobModel.Kind.QUAD, "pig_emperor", 0.9f));
        EntityRendererRegistry.register(ModEntities.ZOMBIE_DANCER, ctx -> new MadMobRenderer<>(ctx, MadModels.ZOMBIE_DANCER, MadMobModel.Kind.BIPED, "zombie_dancer", 0.5f));
        EntityRendererRegistry.register(ModEntities.VILLAGE_MADMAN, ctx -> new MadMobRenderer<>(ctx, MadModels.VILLAGE_MADMAN, MadMobModel.Kind.BIPED, "village_madman", 0.5f));
        EntityRendererRegistry.register(ModEntities.NINJA_CHICKEN, ctx -> new MadMobRenderer<>(ctx, MadModels.NINJA_CHICKEN, MadMobModel.Kind.BIRD, "ninja_chicken", 0.3f));
        EntityRendererRegistry.register(ModEntities.ELITE_SKELETON, ctx -> new MadMobRenderer<>(ctx, MadModels.ELITE_SKELETON, MadMobModel.Kind.BIPED, "elite_skeleton", 0.5f));

        EntityRendererRegistry.register(ModEntities.BANANA_GRENADE, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.GOLDEN_POTATO, FlyingItemEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.ControlsPayload.ID,
                (payload, context) -> invertedTicks = Math.max(0, payload.ticks()));

        // Перевёрнутое управление без миксинов: на время тика меняем состояние клавиш местами, затем возвращаем.
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            swapped = false;
            if (invertedTicks <= 0 || client.player == null) return;
            invertedTicks--;
            if (client.currentScreen != null) return;
            GameOptions o = client.options;
            wasF = o.forwardKey.isPressed();
            wasB = o.backKey.isPressed();
            wasL = o.leftKey.isPressed();
            wasR = o.rightKey.isPressed();
            o.forwardKey.setPressed(wasB);
            o.backKey.setPressed(wasF);
            o.leftKey.setPressed(wasR);
            o.rightKey.setPressed(wasL);
            swapped = true;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!swapped) return;
            GameOptions o = client.options;
            o.forwardKey.setPressed(wasF);
            o.backKey.setPressed(wasB);
            o.leftKey.setPressed(wasL);
            o.rightKey.setPressed(wasR);
            swapped = false;
        });
    }
}
