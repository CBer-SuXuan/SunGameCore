package me.suxuan.sungame.impl;

import me.suxuan.slimearena.api.ArenaManager;
import me.suxuan.sungame.SunGameCorePlugin;
import me.suxuan.sungame.api.queue.QueueArena;
import me.suxuan.sungame.api.queue.QueueCallbacks;
import me.suxuan.sungame.api.queue.QueueJoinResult;
import me.suxuan.sungame.api.queue.QueueJoinStatus;
import me.suxuan.sungame.api.queue.QueueManager;
import me.suxuan.sungame.api.queue.QueueSettings;
import me.suxuan.sungame.api.queue.QueueState;
import me.suxuan.sungame.util.AudienceUtil;
import me.suxuan.sungame.util.LocationUtil;
import me.suxuan.sungame.util.PlayerStateUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class QueueManagerImpl implements QueueManager {
	private final SunGameCorePlugin apiPlugin;
	private final JavaPlugin owner;
	private final ArenaManager arenaManager;
	private final QueueCallbacks callbacks;
	private final List<QueueArena> queues = new ArrayList<>();
	private final Map<UUID, QueueArena> playerQueues = new HashMap<>();
	private QueueSettings settings;
	private boolean creatingQueue;
	private CompletableFuture<QueueArena> pendingQueueCreation;

	public QueueManagerImpl(@NotNull SunGameCorePlugin apiPlugin, @NotNull JavaPlugin owner, @NotNull ArenaManager arenaManager,
	                        @NotNull QueueSettings settings, @NotNull QueueCallbacks callbacks) {
		this.apiPlugin = apiPlugin;
		this.owner = owner;
		this.arenaManager = arenaManager;
		this.settings = settings;
		this.callbacks = callbacks;
	}

	@Override
	public @NotNull CompletableFuture<QueueArena> createQueue() {
		if (creatingQueue && pendingQueueCreation != null) return pendingQueueCreation;
		creatingQueue = true;
		String id = settings.idPrefix() + "_queue_" + System.currentTimeMillis();
		CompletableFuture<QueueArena> future = new CompletableFuture<>();
		pendingQueueCreation = future;
		arenaManager.createArenaAsync(settings.templateWorld(), id).whenComplete((world, throwable) -> Bukkit.getScheduler().runTask(owner, () -> {
			creatingQueue = false;
			pendingQueueCreation = null;
			if (throwable != null) {
				callbacks.onQueueCreateFailed(throwable);
				future.completeExceptionally(throwable);
				return;
			}
			QueueArena queue = new QueueArena(id, world);
			queues.add(queue);
			callbacks.onQueueCreated(queue);
			future.complete(queue);
		}));
		return future;
	}

	@Override
	public void joinQueue(@NotNull Player player) {
		joinQueueResult(player);
	}

	@Override
	public @NotNull CompletableFuture<QueueJoinResult> joinQueueResult(@NotNull Player player) {
		if (!player.isOnline()) return CompletableFuture.completedFuture(QueueJoinResult.fail(QueueJoinStatus.PLAYER_OFFLINE));
		if (playerQueues.containsKey(player.getUniqueId())) {
			return CompletableFuture.completedFuture(QueueJoinResult.fail(QueueJoinStatus.ALREADY_IN_QUEUE, playerQueues.get(player.getUniqueId())));
		}
		QueueArena queue = findJoinableQueue();
		if (queue != null) return CompletableFuture.completedFuture(addToQueueResult(player, queue));
		CompletableFuture<QueueJoinResult> result = new CompletableFuture<>();
		createQueue().whenComplete((created, throwable) -> Bukkit.getScheduler().runTask(owner, () -> {
			if (!player.isOnline()) {
				result.complete(QueueJoinResult.fail(QueueJoinStatus.PLAYER_OFFLINE));
				return;
			}
			if (throwable != null) {
				result.complete(QueueJoinResult.fail(QueueJoinStatus.CREATE_FAILED, throwable));
				return;
			}
			QueueArena available = findJoinableQueue();
			result.complete(addToQueueResult(player, available == null ? created : available));
		}));
		return result;
	}

	@Override
	public boolean addToQueue(@NotNull Player player, @NotNull QueueArena queue) {
		return addToQueueResult(player, queue).success();
	}

	@Override
	public @NotNull QueueJoinResult addToQueueResult(@NotNull Player player, @NotNull QueueArena queue) {
		if (!player.isOnline()) return QueueJoinResult.fail(QueueJoinStatus.PLAYER_OFFLINE, queue);
		if (!queues.contains(queue)) return QueueJoinResult.fail(QueueJoinStatus.QUEUE_NOT_MANAGED, queue);
		if (queue.state() == QueueState.CLOSED) return QueueJoinResult.fail(QueueJoinStatus.QUEUE_CLOSED, queue);
		if (queue.players().size() >= settings.maxPlayers()) return QueueJoinResult.fail(QueueJoinStatus.QUEUE_FULL, queue);
		if (playerQueues.containsKey(player.getUniqueId())) return QueueJoinResult.fail(QueueJoinStatus.ALREADY_IN_QUEUE, playerQueues.get(player.getUniqueId()));
		queue.players().add(player.getUniqueId());
		playerQueues.put(player.getUniqueId(), queue);
		player.getInventory().clear();
		PlayerStateUtil.reset(player);
		PlayerStateUtil.healToMax(player);
		player.setGameMode(GameMode.ADVENTURE);
		player.teleportAsync(LocationUtil.withWorld(settings.spawn(), queue.world()));
		callbacks.onPlayerJoinedQueue(player, queue);
		if (queue.players().size() >= settings.minPlayers() && queue.state() == QueueState.WAITING) {
			startCountdown(queue, false);
		}
		return QueueJoinResult.success(queue);
	}

	@Override
	public boolean leaveQueue(@NotNull Player player) {
		QueueArena queue = playerQueues.remove(player.getUniqueId());
		if (queue == null) return false;
		queue.players().remove(player.getUniqueId());
		callbacks.onPlayerLeftQueue(player, queue);
		if (queue.players().isEmpty()) cleanupQueue(queue, true);
		return true;
	}

	@Override
	public void removePlayer(@NotNull Player player, boolean cleanupEmptyQueue) {
		QueueArena queue = playerQueues.remove(player.getUniqueId());
		if (queue == null) return;
		queue.players().remove(player.getUniqueId());
		callbacks.onPlayerLeftQueue(player, queue);
		if (cleanupEmptyQueue && queue.players().isEmpty()) cleanupQueue(queue, true);
	}

	@Override
	public void startCountdown(@NotNull QueueArena queue, boolean force) {
		if (!queues.contains(queue) || queue.state() == QueueState.CLOSED) return;
		if (queue.countdownTaskId() != -1) {
			if (!force) return;
			Bukkit.getScheduler().cancelTask(queue.countdownTaskId());
			queue.countdownTaskId(-1);
		}
		queue.state(QueueState.STARTING);
		queue.quickCountdown(force);
		queue.countdownLeft(force ? 3 : settings.longCountdownSeconds());
		int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(owner, () -> tickCountdown(queue, force), 0L, 20L);
		queue.countdownTaskId(taskId);
	}

	private void tickCountdown(@NotNull QueueArena queue, boolean force) {
		if (!queues.contains(queue) || queue.state() == QueueState.CLOSED) return;
		if (!force && queue.players().size() < settings.minPlayers()) {
			cancelCountdown(queue);
			callbacks.onCountdownCancelled(queue);
			return;
		}
		if (!queue.quickCountdown() && queue.players().size() >= settings.quickStartPlayers()) {
			queue.quickCountdown(true);
			queue.countdownLeft(Math.min(queue.countdownLeft(), settings.quickCountdownSeconds()));
		}
		if (queue.players().size() >= settings.maxPlayers()) {
			queue.quickCountdown(true);
			queue.countdownLeft(Math.min(queue.countdownLeft(), 3));
		}
		if (queue.countdownLeft() <= 0) {
			Bukkit.getScheduler().cancelTask(queue.countdownTaskId());
			queue.countdownTaskId(-1);
			queue.state(QueueState.CLOSED);
			callbacks.onQueueReady(queue);
			return;
		}
		callbacks.onCountdownTick(queue, queue.countdownLeft());
		queue.countdownLeft(queue.countdownLeft() - 1);
	}

	private void cancelCountdown(@NotNull QueueArena queue) {
		if (queue.countdownTaskId() != -1) Bukkit.getScheduler().cancelTask(queue.countdownTaskId());
		queue.countdownTaskId(-1);
		queue.countdownLeft(-1);
		queue.quickCountdown(false);
		queue.state(QueueState.WAITING);
	}

	@Override
	public void cleanupQueue(@NotNull QueueArena queue, boolean discardWorld) {
		queues.remove(queue);
		if (queue.countdownTaskId() != -1) Bukkit.getScheduler().cancelTask(queue.countdownTaskId());
		for (UUID uuid : new ArrayList<>(queue.players())) {
			playerQueues.remove(uuid);
		}
		queue.players().clear();
		queue.countdownTaskId(-1);
		queue.countdownLeft(-1);
		queue.state(QueueState.CLOSED);
		callbacks.onQueueCleaned(queue);
		if (discardWorld) {
			arenaManager.discardArenaAsync(queue.world(), null).whenComplete((v, throwable) -> {
				if (throwable != null) owner.getLogger().warning("卸载 queue 世界失败: " + throwable.getMessage());
			});
		}
	}

	@Override
	public @NotNull Optional<QueueArena> queueOf(@NotNull Player player) {
		return Optional.ofNullable(playerQueues.get(player.getUniqueId()));
	}

	@Override
	public @NotNull List<QueueArena> queues() {
		return List.copyOf(queues);
	}

	@Override
	public int minPlayers() { return settings.minPlayers(); }

	@Override
	public int maxPlayers() { return settings.maxPlayers(); }

	@Override
	public int quickStartPlayers() { return settings.quickStartPlayers(); }

	@Override
	public @NotNull QueueSettings settings() { return settings; }

	@Override
	public void updateSettings(@NotNull QueueSettings settings) {
		this.settings = settings;
	}

	@Override
	public void stopAll() {
		for (QueueArena queue : new ArrayList<>(queues)) cleanupQueue(queue, true);
	}

	private QueueArena findJoinableQueue() {
		for (QueueArena queue : queues) {
			if ((queue.state() == QueueState.WAITING || queue.state() == QueueState.STARTING) && queue.players().size() < settings.maxPlayers()) {
				return queue;
			}
		}
		return null;
	}
}
