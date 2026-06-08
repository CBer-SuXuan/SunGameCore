package me.suxuan.sungame.api.placeholder;

import me.suxuan.sungame.api.queue.QueueArena;
import me.suxuan.sungame.api.session.GameSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * BaseMiniGameExpansion 查询游戏状态和扩展占位符时使用的接口。
 */
public interface PlaceholderValueProvider<G extends GameSession> {
	G gameOf(@NotNull Player player);
	QueueArena queueOf(@NotNull Player player);
	int minPlayers();
	int maxPlayers();

	default String resolveCustom(@NotNull Player player, G game, QueueArena queue, @NotNull String key) {
		return "";
	}
}
