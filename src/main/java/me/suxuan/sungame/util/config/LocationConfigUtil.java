package me.suxuan.sungame.util.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Location 配置读取工具。
 */
public final class LocationConfigUtil {
	private LocationConfigUtil() {}

	public static @NotNull Location read(@NotNull ConfigurationSection section) {
		World world = null;
		String worldName = section.getString("world");
		if (worldName != null && !worldName.isBlank()) world = Bukkit.getWorld(worldName);
		return new Location(
				world,
				section.getDouble("x"),
				section.getDouble("y"),
				section.getDouble("z"),
				(float) section.getDouble("yaw", 0.0D),
				(float) section.getDouble("pitch", 0.0D)
		);
	}

	public static @NotNull Location readRelative(@NotNull ConfigurationSection section) {
		return new Location(
				null,
				section.getDouble("x"),
				section.getDouble("y"),
				section.getDouble("z"),
				(float) section.getDouble("yaw", 0.0D),
				(float) section.getDouble("pitch", 0.0D)
		);
	}
}
