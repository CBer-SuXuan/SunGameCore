package me.suxuan.sungame.api.spectator;

import me.suxuan.sungame.api.session.GameSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 通用旁观者服务。
 */
public interface SpectatorService<G extends GameSession> {
	/**
	 * 将玩家设置为旁观状态，并根据选项传送、发放离开物品、隐藏给存活玩家。
	 */
	void makeSpectator(@NotNull Player player, @NotNull G game, @NotNull SpectatorOptions options);

	/**
	 * 将旁观者对游戏内所有存活玩家隐藏。
	 */
	void hideFromAlive(@NotNull Player spectator, @NotNull G game);

	/**
	 * 将指定玩家重新显示给同局所有玩家。
	 */
	void showToGame(@NotNull Player target, @NotNull G game);

	/**
	 * 恢复该游戏内所有玩家之间的可见性。
	 */
	void showAll(@NotNull G game);

	/**
	 * 清理单个玩家的旁观者记录，并尝试恢复可见性。
	 */
	void clear(@NotNull Player player);

	/**
	 * 清理该游戏的所有旁观者记录，并恢复可见性。
	 */
	void clearAll(@NotNull G game);
}
