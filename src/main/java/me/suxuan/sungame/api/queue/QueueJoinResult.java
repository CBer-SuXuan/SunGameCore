package me.suxuan.sungame.api.queue;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * 玩家加入队列的详细结果。
 *
 * @param status    加入状态
 * @param queue     相关队列，可能为 null
 * @param throwable 创建队列或加入过程中出现的异常，可能为 null
 */
public record QueueJoinResult(
		@NotNull QueueJoinStatus status,
		@Nullable QueueArena queue,
		@Nullable Throwable throwable
) {
	public boolean success() {
		return status == QueueJoinStatus.SUCCESS;
	}

	public static QueueJoinResult success(@NotNull QueueArena queue) {
		return new QueueJoinResult(QueueJoinStatus.SUCCESS, queue, null);
	}

	public static QueueJoinResult fail(@NotNull QueueJoinStatus status) {
		return new QueueJoinResult(status, null, null);
	}

	public static QueueJoinResult fail(@NotNull QueueJoinStatus status, @Nullable QueueArena queue) {
		return new QueueJoinResult(status, queue, null);
	}

	public static QueueJoinResult fail(@NotNull QueueJoinStatus status, @Nullable Throwable throwable) {
		return new QueueJoinResult(status, null, throwable);
	}
}
