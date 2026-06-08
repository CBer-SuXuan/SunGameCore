package me.suxuan.sungame.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public final class LocationUtil {
	private LocationUtil() {}

	public static @NotNull Location withWorld(@NotNull Location relative, @NotNull World world) {
		return new Location(world, relative.getX(), relative.getY(), relative.getZ(), relative.getYaw(), relative.getPitch());
	}
}
