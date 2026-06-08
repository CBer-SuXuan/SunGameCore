package me.suxuan.sungame.api.session;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 只包含通用字段的基础对局实现。
 */
public class BaseGameSession implements GameSession {
	private final String id;
	private final World world;
	private final String mapId;
	private final Set<UUID> players = new LinkedHashSet<>();
	private final Set<UUID> alivePlayers = new HashSet<>();
	private GameState state = GameState.WAITING;
	private int gameTimerTaskId = -1;
	private int endingTaskId = -1;

	public BaseGameSession(@NotNull String id, @NotNull World world, @NotNull String mapId) {
		this.id = id;
		this.world = world;
		this.mapId = mapId;
	}

	@Override public @NotNull String id() { return id; }
	@Override public @NotNull World world() { return world; }
	@Override public @NotNull String mapId() { return mapId; }
	@Override public @NotNull Set<UUID> players() { return players; }
	@Override public @NotNull Set<UUID> alivePlayers() { return alivePlayers; }
	@Override public @NotNull GameState state() { return state; }
	@Override public void state(@NotNull GameState state) { this.state = state; }
	public int gameTimerTaskId() { return gameTimerTaskId; }
	public void gameTimerTaskId(int gameTimerTaskId) { this.gameTimerTaskId = gameTimerTaskId; }
	public int endingTaskId() { return endingTaskId; }
	public void endingTaskId(int endingTaskId) { this.endingTaskId = endingTaskId; }
}
