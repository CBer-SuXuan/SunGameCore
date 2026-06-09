package me.suxuan.sungame.api.boundary;

import me.suxuan.sungame.api.session.GameSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 玩家触发边界规则后的处理动作。
 */
@FunctionalInterface
public interface BoundaryAction<G extends GameSession> {
	void handle(@NotNull Player player, @NotNull G game, @NotNull String reason);
}
