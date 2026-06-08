package me.suxuan.sungame.api.session;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

/**
 * 通用小游戏对局视图。
 * 具体小游戏可以实现该接口，或继承 BaseGameSession。
 */
public interface GameSession {
	@NotNull String id();
	@NotNull World world();
	@NotNull String mapId();
	@NotNull Set<UUID> players();
	@NotNull Set<UUID> alivePlayers();
	@NotNull GameState state();
	void state(@NotNull GameState state);
}
