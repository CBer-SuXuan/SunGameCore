package me.suxuan.sungame.impl;

import me.suxuan.slimearena.api.ArenaManager;
import me.suxuan.sungame.SunGameCorePlugin;
import me.suxuan.sungame.api.bossbar.BukkitGameBossBarService;
import me.suxuan.sungame.api.bossbar.GameBossBarService;
import me.suxuan.sungame.api.boundary.BoundaryWatcher;
import me.suxuan.sungame.api.boundary.BukkitBoundaryWatcher;
import me.suxuan.sungame.api.cleanup.BukkitGameCleanupService;
import me.suxuan.sungame.api.cleanup.GameCleanupService;
import me.suxuan.sungame.api.MiniGameService;
import me.suxuan.sungame.api.queue.QueueCallbacks;
import me.suxuan.sungame.api.queue.QueueManager;
import me.suxuan.sungame.api.queue.QueueSettings;
import me.suxuan.sungame.api.session.GameSession;
import me.suxuan.sungame.api.spectator.BukkitSpectatorService;
import me.suxuan.sungame.api.spectator.SpectatorService;
import me.suxuan.sungame.api.task.BukkitGameTaskRegistry;
import me.suxuan.sungame.api.task.GameTaskRegistry;
import me.suxuan.sungame.util.TeleportTracker;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class MiniGameServiceImpl implements MiniGameService {
	private final SunGameCorePlugin plugin;
	private final ArenaManager arenaManager;
	private final Set<QueueManagerImpl> queueManagers = ConcurrentHashMap.newKeySet();

	public MiniGameServiceImpl(@NotNull SunGameCorePlugin plugin, @NotNull ArenaManager arenaManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
	}

	@Override
	public @NotNull QueueManager createQueueManager(@NotNull JavaPlugin owner, @NotNull QueueSettings settings, @NotNull QueueCallbacks callbacks) {
		QueueManagerImpl manager = new QueueManagerImpl(plugin, owner, arenaManager, settings, callbacks);
		queueManagers.add(manager);
		return manager;
	}

	@Override
	public @NotNull TeleportTracker createTeleportTracker(@NotNull JavaPlugin owner) {
		return new TeleportTracker(owner);
	}

	@Override
	public @NotNull GameTaskRegistry createTaskRegistry(@NotNull JavaPlugin owner) {
		return new BukkitGameTaskRegistry(owner);
	}

	@Override
	public @NotNull <G extends GameSession> SpectatorService<G> createSpectatorService(@NotNull JavaPlugin owner) {
		return new BukkitSpectatorService<>(owner);
	}

	@Override
	public @NotNull <G extends GameSession> GameBossBarService<G> createBossBarService(@NotNull JavaPlugin owner) {
		return new BukkitGameBossBarService<>();
	}

	@Override
	public @NotNull <G extends GameSession> BoundaryWatcher<G> createBoundaryWatcher(@NotNull JavaPlugin owner, @NotNull GameTaskRegistry taskRegistry) {
		return new BukkitBoundaryWatcher<>(taskRegistry);
	}

	@Override
	public @NotNull <G extends GameSession> GameCleanupService<G> createCleanupService(@NotNull JavaPlugin owner, @NotNull GameTaskRegistry taskRegistry) {
		return new BukkitGameCleanupService<>(owner, arenaManager, taskRegistry);
	}

	public void stopAllQueues() {
		for (QueueManagerImpl manager : queueManagers) {
			manager.stopAll();
		}
		queueManagers.clear();
	}
}
