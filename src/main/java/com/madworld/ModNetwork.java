package com.madworld.net;

import com.madworld.MadWorldMod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public final class ModNetwork {
    private ModNetwork() {}

    /** Клиенту: на ticks тиков поменять местами W/S и A/D. 0 — выключить. */
    public record ControlsPayload(int ticks) implements CustomPayload {
        public static final CustomPayload.Id<ControlsPayload> ID = new CustomPayload.Id<>(MadWorldMod.id("controls"));
        public static final PacketCodec<RegistryByteBuf, ControlsPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.VAR_INT, ControlsPayload::ticks, ControlsPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void init() {
        PayloadTypeRegistry.playS2C().register(ControlsPayload.ID, ControlsPayload.CODEC);
    }
}
