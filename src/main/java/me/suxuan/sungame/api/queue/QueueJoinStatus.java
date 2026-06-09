package me.suxuan.sungame.api.queue;

/**
 * 玩家加入队列的结果状态。
 */
public enum QueueJoinStatus {
	SUCCESS,
	ALREADY_IN_QUEUE,
	PLAYER_OFFLINE,
	QUEUE_FULL,
	QUEUE_CLOSED,
	QUEUE_NOT_MANAGED,
	CREATE_FAILED
}
