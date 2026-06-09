package me.suxuan.sungame.api.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * 基于 Bukkit Scheduler 的 {@link GameTaskRegistry} 实现。
 */
public final class BukkitGameTaskRegistry implements GameTaskRegistry {
	private final JavaPlugin plugin;
	private final Map<TaskKey, Integer> tasks = new HashMap<>();

	public BukkitGameTaskRegistry(@NotNull JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public int runLater(@NotNull String ownerId, @NotNull String taskName, long delayTicks, @NotNull Runnable task) {
		TaskKey key = new TaskKey(ownerId, taskName);
		cancel(key);
		int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			try {
				task.run();
			} finally {
				tasks.remove(key);
			}
		}, Math.max(0L, delayTicks));
		tasks.put(key, taskId);
		return taskId;
	}

	@Override
	public int repeat(@NotNull String ownerId, @NotNull String taskName, long delayTicks, long periodTicks, @NotNull Runnable task) {
		TaskKey key = new TaskKey(ownerId, taskName);
		cancel(key);
		int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, Math.max(0L, delayTicks), Math.max(1L, periodTicks));
		tasks.put(key, taskId);
		return taskId;
	}

	@Override
	public boolean cancel(@NotNull String ownerId, @NotNull String taskName) {
		return cancel(new TaskKey(ownerId, taskName));
	}

	private boolean cancel(TaskKey key) {
		Integer taskId = tasks.remove(key);
		if (taskId == null) return false;
		Bukkit.getScheduler().cancelTask(taskId);
		return true;
	}

	@Override
	public void cancelAll(@NotNull String ownerId) {
		Iterator<Map.Entry<TaskKey, Integer>> iterator = tasks.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<TaskKey, Integer> entry = iterator.next();
			if (!entry.getKey().ownerId().equals(ownerId)) continue;
			Bukkit.getScheduler().cancelTask(entry.getValue());
			iterator.remove();
		}
	}

	@Override
	public void cancelAll() {
		for (Integer taskId : tasks.values()) {
			Bukkit.getScheduler().cancelTask(taskId);
		}
		tasks.clear();
	}

	@Override
	public boolean hasTask(@NotNull String ownerId, @NotNull String taskName) {
		return tasks.containsKey(new TaskKey(ownerId, taskName));
	}

	private record TaskKey(@NotNull String ownerId, @NotNull String taskName) {
		private TaskKey {
			Objects.requireNonNull(ownerId, "ownerId");
			Objects.requireNonNull(taskName, "taskName");
			if (ownerId.isBlank()) throw new IllegalArgumentException("ownerId 不能为空");
			if (taskName.isBlank()) throw new IllegalArgumentException("taskName 不能为空");
		}
	}
}
