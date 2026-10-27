package com.madworld.event;

import com.madworld.util.MadUtil;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class EventContext {
    public final MinecraftServer server;
    public final ChaosEvent event;
    private final List<UUID> playerIds = new ArrayList<>();
    /** UUID существ, которых событие создало или изменило. */
    public final List<UUID> entities = new ArrayList<>();
    /** Произвольная точка события (например, место посылки). */
    public BlockPos pos;
    public ServerWorld posWorld;

    EventContext(MinecraftServer server, ChaosEvent event, List<ServerPlayerEntity> players) {
        this.server = server;
        this.event = event;
        for (ServerPlayerEntity p : players) playerIds.add(p.getUuid());
    }

    /** Игроки, которые на данный момент в сети и живы. */
    public List<ServerPlayerEntity> players() {
        List<ServerPlayerEntity> out = new ArrayList<>();
        for (UUID id : playerIds) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
            if (p != null && p.isAlive() && !p.isSpectator()) out.add(p);
        }
        return out;
    }

    public ServerPlayerEntity randomPlayer() {
        List<ServerPlayerEntity> ps = players();
        return ps.isEmpty() ? null : ps.get(MadUtil.rng().nextInt(ps.size()));
    }

    public Entity find(UUID id) {
        for (ServerWorld w : server.getWorlds()) {
            Entity e = w.getEntity(id);
            if (e != null) return e;
        }
        return null;
    }
}
