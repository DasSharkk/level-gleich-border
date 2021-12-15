package net.axay.levelborder.vanilla;

import net.axay.levelborder.common.LevelBorderHandler;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;

import java.util.UUID;

public class VanillaLevelBorderHandler extends LevelBorderHandler<ServerPlayer, WorldBorder> {
    @Override
    protected WorldBorder createWorldBorder(ServerPlayer player) {
        return new WorldBorder();
    }

    @Override
    protected void initBorder(ServerPlayer player, WorldBorder border, double posX, double posZ, double size) {
        border.setCenter(posX, posZ);
        border.setSize(size);
        player.connection.send(new ClientboundInitializeBorderPacket(border));
    }

    @Override
    protected void interpolateBorder(ServerPlayer player, WorldBorder border, double size, long time) {
        border.lerpSizeBetween(border.getSize(), size, time);
        player.connection.send(new ClientboundSetBorderLerpSizePacket(border));
    }

    @Override
    protected int getExperienceLevels(ServerPlayer player) {
        return player.experienceLevel;
    }

    @Override
    protected UUID getUUID(ServerPlayer player) {
        return player.getUUID();
    }
}