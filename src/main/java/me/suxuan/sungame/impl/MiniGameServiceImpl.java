package me.suxuan.sungame.impl;

import me.suxuan.slimearena.api.ArenaManager;
import me.suxuan.sungame.SunGameCorePlugin;
import me.suxuan.sungame.api.MiniGameService;
import me.suxuan.sungame.api.queue.QueueCallbacks;
import me.suxuan.sungame.api.queue.QueueManager;
import me.suxuan.sungame.api.queue.QueueSettings;
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

	public void stopAllQueues() {
		for (QueueManagerImpl manager : queueManagers) {
			manager.stopAll();
		}
		queueManagers.clear();
	}
}
