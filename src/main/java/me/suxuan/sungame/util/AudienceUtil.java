package me.suxuan.sungame.util;

import me.suxuan.sungame.api.queue.QueueArena;
import me.suxuan.sungame.api.session.GameSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public final class AudienceUtil {
	private AudienceUtil() {}

	public static void broadcastQueue(@NotNull QueueArena queue, @NotNull Component component) {
		for (UUID uuid : queue.players()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) player.sendMessage(component);
		}
	}

	public static void broadcastGame(@NotNull GameSession game, @NotNull Component component) {
		for (UUID uuid : game.players()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) player.sendMessage(component);
		}
	}

	public static void showQueueTitle(@NotNull QueueArena queue, @NotNull Component title, @NotNull Component subtitle,
	                                  int fadeInTicks, int stayTicks, int fadeOutTicks) {
		Title titleObject = title(title, subtitle, fadeInTicks, stayTicks, fadeOutTicks);
		for (UUID uuid : queue.players()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) player.showTitle(titleObject);
		}
	}

	public static void showGameTitle(@NotNull GameSession game, @NotNull Component title, @NotNull Component subtitle,
	                                 int fadeInTicks, int stayTicks, int fadeOutTicks) {
		Title titleObject = title(title, subtitle, fadeInTicks, stayTicks, fadeOutTicks);
		for (UUID uuid : game.players()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) player.showTitle(titleObject);
		}
	}

	private static Title title(Component title, Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
		return Title.title(title, subtitle, Title.Times.times(
				Duration.ofMillis(fadeInTicks * 50L),
				Duration.ofMillis(stayTicks * 50L),
				Duration.ofMillis(fadeOutTicks * 50L)
		));
	}
}
