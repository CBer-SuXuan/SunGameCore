package me.suxuan.sungame.api.task;

import me.suxuan.sungame.api.session.GameSession;
import org.jetbrains.annotations.NotNull;

/**
 * 通用小游戏任务注册表。
 * <p>
 * 用 ownerId + taskName 标识一个 Bukkit task，适合为单局游戏管理保护期、游戏时间、边界检测、结束延迟等任务。
 */
public interface GameTaskRegistry {
	/**
	 * 延迟执行一个任务。如果同名任务已存在，会先取消旧任务。
	 *
	 * @param ownerId    任务归属 ID，通常使用 game.id()
	 * @param taskName   任务名称，例如 protection、timer、boundary
	 * @param delayTicks 延迟 tick
	 * @param task       执行内容
	 * @return Bukkit task id
	 */
	int runLater(@NotNull String ownerId, @NotNull String taskName, long delayTicks, @NotNull Runnable task);

	/**
	 * 重复执行一个任务。如果同名任务已存在，会先取消旧任务。
	 *
	 * @param ownerId    任务归属 ID，通常使用 game.id()
	 * @param taskName   任务名称，例如 protection、timer、boundary
	 * @param delayTicks 首次执行延迟 tick
	 * @param periodTicks 重复间隔 tick
	 * @param task       执行内容
	 * @return Bukkit task id
	 */
	int repeat(@NotNull String ownerId, @NotNull String taskName, long delayTicks, long periodTicks, @NotNull Runnable task);

	/**
	 * 取消指定任务。
	 *
	 * @return true 如果存在并已取消
	 */
	boolean cancel(@NotNull String ownerId, @NotNull String taskName);

	/**
	 * 取消指定 ownerId 下所有任务。
	 */
	void cancelAll(@NotNull String ownerId);

	/**
	 * 取消该注册表管理的所有任务。
	 */
	void cancelAll();

	/**
	 * 判断指定任务是否存在。
	 */
	boolean hasTask(@NotNull String ownerId, @NotNull String taskName);

	default int runLater(@NotNull GameSession game, @NotNull String taskName, long delayTicks, @NotNull Runnable task) {
		return runLater(game.id(), taskName, delayTicks, task);
	}

	default int repeat(@NotNull GameSession game, @NotNull String taskName, long delayTicks, long periodTicks, @NotNull Runnable task) {
		return repeat(game.id(), taskName, delayTicks, periodTicks, task);
	}

	default boolean cancel(@NotNull GameSession game, @NotNull String taskName) {
		return cancel(game.id(), taskName);
	}

	default void cancelAll(@NotNull GameSession game) {
		cancelAll(game.id());
	}

	default boolean hasTask(@NotNull GameSession game, @NotNull String taskName) {
		return hasTask(game.id(), taskName);
	}
}
