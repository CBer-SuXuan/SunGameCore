package me.suxuan.sungame.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 追踪插件内部发起的异步传送，避免位置检查或换世界监听误判玩家主动离开竞技场。
 */
public final class TeleportTracker {
	private final JavaPlugin plugin;
	private final Map<UUID, String> pendingTargets = new ConcurrentHashMap<>();

	public TeleportTracker(@NotNull JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void teleport(@NotNull Player player, @NotNull Location target) {
		World targetWorld = target.getWorld();
		if (targetWorld == null) {
			plugin.getLogger().warning("无法传送玩家 " + player.getName() + "：目标位置没有世界");
			return;
		}
		UUID uuid = player.getUniqueId();
		pendingTargets.put(uuid, targetWorld.getName());
		player.teleportAsync(target).whenComplete((success, throwable) -> Bukkit.getScheduler().runTask(plugin, () -> {
			String pendingTarget = pendingTargets.get(uuid);
			if (targetWorld.getName().equals(pendingTarget)) pendingTargets.remove(uuid);
			if (throwable != null) {
				plugin.getLogger().warning("异步传送玩家 " + player.getName() + " 到 " + targetWorld.getName() + " 时发生异常: " + throwable.getMessage());
				return;
			}
			if (!Boolean.TRUE.equals(success)) {
				plugin.getLogger().warning("异步传送玩家 " + player.getName() + " 到 " + targetWorld.getName() + " 失败");
			}
		}));
	}

	public boolean isPending(@NotNull Player player, @NotNull World targetWorld) {
		return targetWorld.getName().equals(pendingTargets.get(player.getUniqueId()));
	}

	public void clear(@NotNull Player player) {
		pendingTargets.remove(player.getUniqueId());
	}

	public void clearAll() {
		pendingTargets.clear();
	}
}
