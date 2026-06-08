package me.suxuan.sungame.api.queue;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 一个临时 queue 世界实例。
 */
public final class QueueArena {
	private final String id;
	private final World world;
	private final Set<UUID> players = new LinkedHashSet<>();
	private QueueState state = QueueState.WAITING;
	private int countdownTaskId = -1;
	private int countdownLeft = -1;
	private boolean quickCountdown;

	public QueueArena(@NotNull String id, @NotNull World world) {
		this.id = id;
		this.world = world;
	}

	public String id() { return id; }
	public World world() { return world; }
	public Set<UUID> players() { return players; }
	public QueueState state() { return state; }
	public void state(@NotNull QueueState state) { this.state = state; }
	public int countdownTaskId() { return countdownTaskId; }
	public void countdownTaskId(int countdownTaskId) { this.countdownTaskId = countdownTaskId; }
	public int countdownLeft() { return countdownLeft; }
	public void countdownLeft(int countdownLeft) { this.countdownLeft = countdownLeft; }
	public boolean quickCountdown() { return quickCountdown; }
	public void quickCountdown(boolean quickCountdown) { this.quickCountdown = quickCountdown; }
}
