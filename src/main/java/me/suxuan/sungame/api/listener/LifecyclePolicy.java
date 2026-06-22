package me.suxuan.sungame.api.listener;

import me.suxuan.sungame.api.session.GameSession;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 通用生命周期监听器的行为策略。
 * <p>
 * SunGameCore 不应替具体小游戏决定玩家进服、死亡、重生、饥饿、传送等玩法行为。
 * 使用方应显式传入本策略，即使只使用默认行为，也需要传入 {@link #defaults()}。
 */
public interface LifecyclePolicy<G extends GameSession> {
	static <G extends GameSession> @NotNull LifecyclePolicy<G> defaults() {
		return new LifecyclePolicy<>() {};
	}

	default boolean hideJoinMessage(@NotNull Player player) { return true; }
	default boolean hideQuitMessage(@NotNull Player player) { return true; }

	default boolean clearInventoryOnJoin(@NotNull Player player) { return true; }
	default boolean resetPlayerOnJoin(@NotNull Player player) { return true; }

	default boolean cancelFoodChange(@NotNull Player player) { return true; }
	default int foodLevel(@NotNull Player player) { return 20; }
	default float saturation(@NotNull Player player) { return 20.0F; }

	default boolean cancelNaturalRegain(@NotNull Player player, @NotNull EntityRegainHealthEvent.RegainReason reason) {
		return reason == EntityRegainHealthEvent.RegainReason.SATIATED
				|| reason == EntityRegainHealthEvent.RegainReason.REGEN;
	}

	default boolean cancelPortal(@NotNull Player player) { return true; }

	default boolean cancelTeleport(@NotNull Player player, @NotNull PlayerTeleportEvent.TeleportCause cause) {
		return cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
				|| cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
				|| cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
				|| cause == PlayerTeleportEvent.TeleportCause.END_PORTAL;
	}

	default boolean clearDeathDrops(@NotNull Player player, @NotNull G game) { return true; }
	default boolean clearDroppedExp(@NotNull Player player, @NotNull G game) { return true; }
	default boolean hideDeathMessage(@NotNull Player player, @NotNull G game) { return true; }
	default @NotNull String deathEliminateReason(@NotNull Player player, @NotNull G game) { return "死亡"; }

	default boolean autoRespawn(@NotNull Player player, @NotNull G game) { return true; }
	default long autoRespawnDelayTicks(@NotNull Player player, @NotNull G game) { return 2L; }

	default boolean prepareSpectatorLikeOnRespawn(@NotNull Player player, @NotNull G game) { return true; }
}
