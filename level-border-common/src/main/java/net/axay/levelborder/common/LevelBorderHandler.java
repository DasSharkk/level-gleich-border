package net.axay.levelborder.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class LevelBorderHandler<Player, WorldBorder, Server> {
    private final Map<UUID, WorldBorder> borders = new HashMap<>();

    private double calculateSize(Player player) {
        final int experience = switch (getMode()) {
            case OWN, SHARED -> getExperienceLevel(player);
            case SUM -> getPlayers().stream()
                .map(this::getExperienceLevel).reduce(0, Integer::sum);
        };
        return Math.max(experience * 2.0D, 1.0D);
    }

    final public void initBorder(Player player) {
        final var spawn = sharedOverworldSpawn();

        final var border = createWorldBorder(player);
        setCenter(border, spawn.x() + 0.5d, spawn.z() + 0.5d);
        initBorder(player, border, calculateSize(player));
        borders.put(getUUID(player), border);

        for (Player onlinePlayer : getPlayers()) {
            if (onlinePlayer != player) {
                updateWorldBorder(onlinePlayer);
            }
        }
    }

    final public void onChangeLevel(Player player) {
        if (getMode() == BorderMode.SHARED) {
            final var experience = getPlayers().stream().map(this::getExperienceLevel)
                .max(Integer::compare).orElse(0);
            getPlayers().forEach(onlinePlayer -> {
                setExperienceLevel(onlinePlayer, experience);
                updateWorldBorder(onlinePlayer);
            });
        } else {
            updateWorldBorder(player);
        }
    }

    final public void updateWorldBorder(Player player) {
        final var border = borders.get(getUUID(player));
        if (border != null) {
            interpolateBorder(player, border, calculateSize(player), 2L * 1000L);
        }
    }

    final public void checkOutsideBorder(Player player) {
        final var border = borders.get(getUUID(player));
        if (border != null) {
            if (getDistance(player, border) + 5.0d < 0) {
                hurt(player, 1f);
            }
        }
    }

    final public void onLeave(Player player) {
        borders.remove(getUUID(player));
        getPlayers().forEach(this::updateWorldBorder);
    }

    final public Pos3i getRespawnPos() {
        return sharedOverworldSpawn();
    }

    public void setMode(BorderMode mode) {
        getPlayers().forEach(this::updateWorldBorder);
    }

    abstract protected BorderMode getMode();

    abstract protected WorldBorder createWorldBorder(Player player);
    abstract protected void setCenter(WorldBorder border, double centerX, double centerZ);

    abstract protected void initBorder(Player player, WorldBorder border, double size);
    abstract protected void interpolateBorder(Player player, WorldBorder border, double size, long time);

    abstract protected Server getServer();
    abstract protected Collection<Player> getPlayers();

    abstract protected double getDistance(Player player, WorldBorder border);

    abstract protected Pos3i sharedOverworldSpawn();
    abstract protected int getExperienceLevel(Player player);
    abstract protected void setExperienceLevel(Player player, int level);
    abstract protected UUID getUUID(Player player);
    abstract protected void hurt(Player player, float damage);
}
