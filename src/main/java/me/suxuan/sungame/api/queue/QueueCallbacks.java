package me.suxuan.sungame.api.queue;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * QueueManager 的事件回调。
 * 默认方法均为空，小游戏只需重写自己关心的部分。
 */
public interface QueueCallbacks {
	default void onQueueCreated(@NotNull QueueArena queue) {}
	default void onPlayerJoinedQueue(@NotNull Player player, @NotNull QueueArena queue) {}
	default void onPlayerLeftQueue(@NotNull Player player, @NotNull QueueArena queue) {}
	default void onCountdownTick(@NotNull QueueArena queue, int secondsLeft) {}
	default void onCountdownCancelled(@NotNull QueueArena queue) {}
	default void onQueueReady(@NotNull QueueArena queue) {}
	default void onQueueCleaned(@NotNull QueueArena queue) {}
	default void onQueueCreateFailed(@NotNull Throwable throwable) {}
}
