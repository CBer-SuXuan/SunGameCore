package me.suxuan.sungame.api.boundary;

import me.suxuan.sungame.api.session.GameSession;
import me.suxuan.sungame.util.TeleportTracker;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 游戏边界规则。返回空表示玩家未触发该规则，返回原因表示触发违规。
 */
@FunctionalInterface
public interface BoundaryRule<G extends GameSession> {
	@NotNull Optional<String> check(@NotNull Player player, @NotNull G game);

	static <G extends GameSession> @NotNull BoundaryRule<G> belowY(double minY, @NotNull String reason) {
		return (player, game) -> player.getWorld().equals(game.world()) && player.getLocation().getY() <= minY
				? Optional.of(reason)
				: Optional.empty();
	}

	static <G extends GameSession> @NotNull BoundaryRule<G> outsideWorld(@NotNull String reason) {
		return (player, game) -> player.getWorld().equals(game.world())
				? Optional.empty()
				: Optional.of(reason);
	}

	static <G extends GameSession> @NotNull BoundaryRule<G> outsideWorldIgnoringPendingTeleport(@NotNull TeleportTracker teleportTracker, @NotNull String reason) {
		return (player, game) -> {
			if (player.getWorld().equals(game.world())) return Optional.empty();
			if (teleportTracker.isPending(player, game.world())) return Optional.empty();
			return Optional.of(reason);
		};
	}

	static <G extends GameSession> @NotNull BoundaryRule<G> outsideBox(@NotNull Location min, @NotNull Location max, @NotNull String reason) {
		Location safeMin = min.clone();
		Location safeMax = max.clone();
		double minX = Math.min(safeMin.getX(), safeMax.getX());
		double maxX = Math.max(safeMin.getX(), safeMax.getX());
		double minY = Math.min(safeMin.getY(), safeMax.getY());
		double maxY = Math.max(safeMin.getY(), safeMax.getY());
		double minZ = Math.min(safeMin.getZ(), safeMax.getZ());
		double maxZ = Math.max(safeMin.getZ(), safeMax.getZ());
		return (player, game) -> {
			if (!player.getWorld().equals(game.world())) return Optional.empty();
			Location location = player.getLocation();
			boolean outside = location.getX() < minX || location.getX() > maxX
					|| location.getY() < minY || location.getY() > maxY
					|| location.getZ() < minZ || location.getZ() > maxZ;
			return outside ? Optional.of(reason) : Optional.empty();
		};
	}

	static <G extends GameSession> @NotNull BoundaryRule<G> outsideRadius(@NotNull Location center, double radius, @NotNull String reason) {
		Location safeCenter = center.clone();
		double radiusSquared = Math.max(0.0D, radius) * Math.max(0.0D, radius);
		return (player, game) -> {
			if (!player.getWorld().equals(game.world())) return Optional.empty();
			Location actualCenter = safeCenter.clone();
			World gameWorld = game.world();
			if (actualCenter.getWorld() == null || !actualCenter.getWorld().equals(gameWorld)) actualCenter.setWorld(gameWorld);
			return player.getLocation().distanceSquared(actualCenter) > radiusSquared ? Optional.of(reason) : Optional.empty();
		};
	}
}
