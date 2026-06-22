package me.suxuan.sungame.api.listener;

import me.suxuan.sungame.api.session.GameSession;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 通用生命周期监听器使用的回调。
 */
public interface LifecycleCallbacks<G extends GameSession> {
	/**
	 * 自定义处理玩家进服。
	 *
	 * @return true 表示已处理完进服逻辑，CommonLifecycleListener 不再执行默认 OP/自动入队逻辑
	 */
	default boolean handleJoin(@NotNull Player player) { return false; }

	default boolean autoJoinQueue(@NotNull Player player) { return true; }
	void joinQueue(@NotNull Player player);
	void handleQuit(@NotNull Player player);
	void eliminate(@NotNull Player player, @NotNull String reason);
	default Location respawnLocation(@NotNull Player player, @NotNull G game) { return player.getLocation().clone(); }
	default void afterRespawn(@NotNull Player player, @NotNull G game) {}
}
