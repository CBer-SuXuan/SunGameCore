package me.suxuan.sungame.api.cleanup;

import me.suxuan.slimearena.api.ArenaManager;
import me.suxuan.sungame.api.session.GameSession;
import me.suxuan.sungame.api.task.GameTaskRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 基于 Bukkit Scheduler 与 ArenaManager 的通用游戏清理服务。
 */
public final class BukkitGameCleanupService<G extends GameSession> implements GameCleanupService<G> {
	public static final String CLEANUP_TASK = "cleanup";
	public static final String DISCARD_WORLD_TASK = "discard-world";

	private final JavaPlugin plugin;
	private final ArenaManager arenaManager;
	private final GameTaskRegistry taskRegistry;

	public BukkitGameCleanupService(@NotNull JavaPlugin plugin, @NotNull ArenaManager arenaManager, @NotNull GameTaskRegistry taskRegistry) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
		this.taskRegistry = taskRegistry;
	}

	@Override
	public void cleanup(@NotNull G game,
	                    @NotNull GameCleanupOptions options,
	                    @NotNull PlayerCleanupAction<G> playerAction,
	                    @NotNull Runnable beforePlayers,
	                    @NotNull Runnable afterPlayers) {
		taskRegistry.runLater(game, CLEANUP_TASK, options.cleanupDelayTicks(), () -> {
			beforePlayers.run();
			for (UUID uuid : new ArrayList<>(game.players())) {
				Player player = Bukkit.getPlayer(uuid);
				if (player != null && player.isOnline()) playerAction.cleanup(player, game);
			}
			afterPlayers.run();
			if (options.discardWorld()) scheduleWorldDiscard(game, options);
			else if (options.cancelAllTasksAfterDiscard()) taskRegistry.cancelAll(game);
		});
	}

	private void scheduleWorldDiscard(@NotNull G game, @NotNull GameCleanupOptions options) {
		taskRegistry.runLater(game, DISCARD_WORLD_TASK, options.discardWorldDelayTicks(), () -> {
			Location fallback = options.fallbackLocation();
			arenaManager.discardArenaAsync(game.world(), fallback).whenComplete((ignored, throwable) -> Bukkit.getScheduler().runTask(plugin, () -> {
				if (throwable != null) plugin.getLogger().warning("卸载游戏世界 " + game.world().getName() + " 失败: " + throwable.getMessage());
				if (options.cancelAllTasksAfterDiscard()) taskRegistry.cancelAll(game);
			}));
		});
	}
}
