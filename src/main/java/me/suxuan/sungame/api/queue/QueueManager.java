package me.suxuan.sungame.api.queue;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface QueueManager {
	@NotNull CompletableFuture<QueueArena> createQueue();
	void joinQueue(@NotNull Player player);
	@NotNull CompletableFuture<QueueJoinResult> joinQueueResult(@NotNull Player player);
	boolean addToQueue(@NotNull Player player, @NotNull QueueArena queue);
	@NotNull QueueJoinResult addToQueueResult(@NotNull Player player, @NotNull QueueArena queue);
	boolean leaveQueue(@NotNull Player player);
	void removePlayer(@NotNull Player player, boolean cleanupEmptyQueue);
	void startCountdown(@NotNull QueueArena queue, boolean force);
	void cleanupQueue(@NotNull QueueArena queue, boolean discardWorld);
	@NotNull Optional<QueueArena> queueOf(@NotNull Player player);
	@NotNull List<QueueArena> queues();
	int minPlayers();
	int maxPlayers();
	int quickStartPlayers();
	@NotNull QueueSettings settings();
	void updateSettings(@NotNull QueueSettings settings);
	void stopAll();
}
