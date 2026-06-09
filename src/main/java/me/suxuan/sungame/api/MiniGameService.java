package me.suxuan.sungame.api;

import me.suxuan.sungame.api.bossbar.GameBossBarService;
import me.suxuan.sungame.api.boundary.BoundaryWatcher;
import me.suxuan.sungame.api.cleanup.GameCleanupService;
import me.suxuan.sungame.api.queue.QueueCallbacks;
import me.suxuan.sungame.api.queue.QueueManager;
import me.suxuan.sungame.api.queue.QueueSettings;
import me.suxuan.sungame.api.session.GameSession;
import me.suxuan.sungame.api.spectator.SpectatorService;
import me.suxuan.sungame.api.task.GameTaskRegistry;
import me.suxuan.sungame.util.TeleportTracker;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * SunGameAPI 暴露给小游戏插件的主服务。
 */
public interface MiniGameService {

	/**
	 * 为指定小游戏插件创建一个独立的队列管理器。
	 * 每个小游戏都应该传入自己的 QueueSettings，因此人数、倒计时和模板世界不会互相影响。
	 *
	 * @param owner     使用该队列管理器的小游戏插件
	 * @param settings  该小游戏独立的队列配置
	 * @param callbacks 队列事件回调
	 * @return 新创建的队列管理器
	 */
	@NotNull
	QueueManager createQueueManager(@NotNull JavaPlugin owner, @NotNull QueueSettings settings, @NotNull QueueCallbacks callbacks);

	/**
	 * 创建一个异步传送追踪器，用于避免内部传送被误判为离开竞技场。
	 *
	 * @param owner 使用该追踪器的小游戏插件
	 * @return 传送追踪器
	 */
	@NotNull
	TeleportTracker createTeleportTracker(@NotNull JavaPlugin owner);

	/**
	 * 创建一个通用任务注册表，用于按对局 ID 管理 Bukkit task。
	 *
	 * @param owner 使用该注册表的小游戏插件
	 * @return 任务注册表
	 */
	@NotNull
	GameTaskRegistry createTaskRegistry(@NotNull JavaPlugin owner);

	/**
	 * 创建一个通用旁观者服务。
	 *
	 * @param owner 使用该服务的小游戏插件
	 * @return 旁观者服务
	 */
	@NotNull
	<G extends GameSession> SpectatorService<G> createSpectatorService(@NotNull JavaPlugin owner);

	/**
	 * 创建一个通用 BossBar 服务。
	 *
	 * @param owner 使用该服务的小游戏插件
	 * @return BossBar 服务
	 */
	@NotNull
	<G extends GameSession> GameBossBarService<G> createBossBarService(@NotNull JavaPlugin owner);

	/**
	 * 创建一个通用边界检测器。
	 *
	 * @param owner        使用该检测器的小游戏插件
	 * @param taskRegistry 该检测器复用的任务注册表
	 * @return 边界检测器
	 */
	@NotNull
	<G extends GameSession> BoundaryWatcher<G> createBoundaryWatcher(@NotNull JavaPlugin owner, @NotNull GameTaskRegistry taskRegistry);

	/**
	 * 创建一个通用游戏清理服务。
	 *
	 * @param owner        使用该服务的小游戏插件
	 * @param taskRegistry 该服务复用的任务注册表
	 * @return 游戏清理服务
	 */
	@NotNull
	<G extends GameSession> GameCleanupService<G> createCleanupService(@NotNull JavaPlugin owner, @NotNull GameTaskRegistry taskRegistry);
}
