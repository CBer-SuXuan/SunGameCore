package me.suxuan.sungame.api;

import me.suxuan.sungame.api.queue.QueueCallbacks;
import me.suxuan.sungame.api.queue.QueueManager;
import me.suxuan.sungame.api.queue.QueueSettings;
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
}
