package me.suxuan.sungame.hook;

import me.suxuan.sungame.SunGameCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Handles best-effort cleanup for temporary Slime worlds that WorldGuard may cache.
 *
 * <p>WorldGuard is a soft dependency, so all WorldGuard/WorldEdit API calls are made through reflection.</p>
 */
public final class WorldGuardHook {
	private final SunGameCorePlugin plugin;

	public WorldGuardHook(@NotNull SunGameCorePlugin plugin) {
		this.plugin = plugin;
	}

	public boolean isAvailable() {
		Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
		return worldGuard != null && worldGuard.isEnabled();
	}

	public void cleanupTransientWorld(@NotNull World world) {
		cleanupTransientWorld(world, false);
	}

	public void cleanupTransientWorld(@NotNull World world, boolean afterBukkitUnload) {
		if (!plugin.getConfig().getBoolean("worldguard.cleanup-transient-worlds", true)) return;
		String worldName = world.getName();
		unloadRegionManager(world);
		if (afterBukkitUnload) cleanupWorldGuardDataFolder(worldName);
	}

	public void cleanupWorldGuardDataFolder(@NotNull String worldName) {
		if (!plugin.getConfig().getBoolean("worldguard.cleanup-transient-worlds", true)) return;
		Path worldFolder = plugin.getServer().getWorldContainer().toPath()
				.resolve("plugins")
				.resolve("WorldGuard")
				.resolve("worlds")
				.resolve(worldName);
		if (!Files.exists(worldFolder)) return;
		boolean deleteTransientWorldFolders = plugin.getConfig().getBoolean("worldguard.delete-transient-world-folders", true);
		try {
			if (deleteTransientWorldFolders) {
				deleteRecursively(worldFolder);
				plugin.log("<gray>已删除 WorldGuard 临时世界数据目录: <aqua>" + worldName + "</aqua></gray>");
			}
		} catch (IOException exception) {
			plugin.getLogger().warning("清理 WorldGuard 临时世界目录失败: " + worldName + " - " + exception.getMessage());
		}
	}

	private void unloadRegionManager(@NotNull World world) {
		if (!isAvailable()) return;
		try {
			Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
			Object worldGuard = worldGuardClass.getMethod("getInstance").invoke(null);
			Object platform = worldGuardClass.getMethod("getPlatform").invoke(worldGuard);
			Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);

			Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
			Method adaptWorldMethod = bukkitAdapterClass.getMethod("adapt", World.class);
			Object adaptedWorld = adaptWorldMethod.invoke(null, world);

			Method unloadMethod = findSingleArgumentMethod(regionContainer.getClass(), "unload", adaptedWorld.getClass());
			if (unloadMethod == null) throw new NoSuchMethodException("RegionContainer#unload(" + adaptedWorld.getClass().getName() + ")");
			unloadMethod.invoke(regionContainer, adaptedWorld);
			plugin.log("<gray>已卸载 WorldGuard 在 <aqua>" + world.getName() + "</aqua> 的区划缓存。</gray>");
		} catch (Throwable throwable) {
			plugin.getLogger().fine("卸载 WorldGuard 区划缓存失败: " + world.getName() + " - " + throwable.getMessage());
		}
	}

	private Method findSingleArgumentMethod(@NotNull Class<?> type, @NotNull String name, @NotNull Class<?> argumentType) {
		for (Method method : type.getMethods()) {
			if (!method.getName().equals(name) || method.getParameterCount() != 1) continue;
			if (method.getParameterTypes()[0].isAssignableFrom(argumentType)) return method;
		}
		return null;
	}


	private void deleteRecursively(@NotNull Path path) throws IOException {
		if (!Files.exists(path)) return;
		try (var paths = Files.walk(path)) {
			for (Path current : paths.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(current);
			}
		}
	}
}
