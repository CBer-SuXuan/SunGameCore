package me.suxuan.sungame.api.cleanup;

import me.suxuan.sungame.api.session.GameSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 游戏清理时对单个在线玩家执行的动作。
 */
@FunctionalInterface
public interface PlayerCleanupAction<G extends GameSession> {
	void cleanup(@NotNull Player player, @NotNull G game);
}
