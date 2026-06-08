package me.suxuan.sungame.api.listener;

import me.suxuan.sungame.api.queue.QueueArena;
import me.suxuan.sungame.api.session.GameSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 通用聊天隔离监听器的开关策略。
 * 默认会隔离正式游戏聊天和 queue 聊天，并且不让控制台接收被隔离的聊天。
 */
public interface ChatPolicy<G extends GameSession> {
	/**
	 * 是否将正式游戏内聊天限制在当前对局内。
	 */
	default boolean isolateGameChat(@NotNull Player player, @NotNull G game) { return true; }

	/**
	 * 是否将 queue 内聊天限制在当前队列内。
	 */
	default boolean isolateQueueChat(@NotNull Player player, @NotNull QueueArena queue) { return true; }

	/**
	 * 正式游戏聊天被隔离时，是否保留控制台和其他非玩家 Audience 接收者。
	 */
	default boolean allowConsoleSeeGameChat(@NotNull Player player, @NotNull G game) { return false; }

	/**
	 * queue 聊天被隔离时，是否保留控制台和其他非玩家 Audience 接收者。
	 */
	default boolean allowConsoleSeeQueueChat(@NotNull Player player, @NotNull QueueArena queue) { return false; }
}
