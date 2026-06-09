package me.suxuan.sungame.api.cleanup;

import me.suxuan.sungame.api.session.GameSession;
import org.jetbrains.annotations.NotNull;

/**
 * 通用游戏清理服务。
 */
public interface GameCleanupService<G extends GameSession> {
	/**
	 * 延迟清理游戏内玩家，并可选延迟卸载游戏世界。
	 *
	 * @param game          目标游戏
	 * @param options       清理选项
	 * @param playerAction  对每个在线玩家执行的清理动作
	 * @param beforePlayers 玩家清理前执行，可用于恢复可见性
	 * @param afterPlayers  玩家清理后执行，可用于移除映射、清理旁观者记录
	 */
	void cleanup(@NotNull G game,
	             @NotNull GameCleanupOptions options,
	             @NotNull PlayerCleanupAction<G> playerAction,
	             @NotNull Runnable beforePlayers,
	             @NotNull Runnable afterPlayers);

	default void cleanup(@NotNull G game,
	                     @NotNull GameCleanupOptions options,
	                     @NotNull PlayerCleanupAction<G> playerAction) {
		cleanup(game, options, playerAction, () -> {}, () -> {});
	}
}
