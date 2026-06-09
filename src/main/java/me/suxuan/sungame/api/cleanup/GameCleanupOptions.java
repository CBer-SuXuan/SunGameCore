package me.suxuan.sungame.api.cleanup;

import org.bukkit.Location;

import javax.annotation.Nullable;

/**
 * 游戏清理选项。
 *
 * @param cleanupDelayTicks          执行玩家清理前的延迟
 * @param discardWorldDelayTicks     玩家清理后卸载世界的延迟
 * @param fallbackLocation           卸载世界时残留玩家的安全位置，可为 null
 * @param discardWorld               是否卸载游戏世界
 * @param cancelAllTasksAfterDiscard 卸载世界后是否清理该游戏所有 task
 */
public record GameCleanupOptions(
		long cleanupDelayTicks,
		long discardWorldDelayTicks,
		@Nullable Location fallbackLocation,
		boolean discardWorld,
		boolean cancelAllTasksAfterDiscard
) {
	public GameCleanupOptions {
		cleanupDelayTicks = Math.max(0L, cleanupDelayTicks);
		discardWorldDelayTicks = Math.max(0L, discardWorldDelayTicks);
		if (fallbackLocation != null) fallbackLocation = fallbackLocation.clone();
	}

	public static GameCleanupOptions basic(long cleanupDelayTicks) {
		return new GameCleanupOptions(cleanupDelayTicks, 0L, null, false, true);
	}

	public static GameCleanupOptions discardWorld(long cleanupDelayTicks, long discardWorldDelayTicks, @Nullable Location fallbackLocation) {
		return new GameCleanupOptions(cleanupDelayTicks, discardWorldDelayTicks, fallbackLocation, true, true);
	}

	public static GameCleanupOptions discardWorld(long cleanupDelayTicks, long discardWorldDelayTicks, @Nullable Location fallbackLocation, boolean cancelAllTasksAfterDiscard) {
		return new GameCleanupOptions(cleanupDelayTicks, discardWorldDelayTicks, fallbackLocation, true, cancelAllTasksAfterDiscard);
	}

	@Override
	public Location fallbackLocation() {
		return fallbackLocation == null ? null : fallbackLocation.clone();
	}
}
