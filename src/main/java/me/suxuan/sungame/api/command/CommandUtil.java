package me.suxuan.sungame.api.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * 命令工具方法。
 */
public final class CommandUtil {
	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

	private CommandUtil() {}

	public static @NotNull Component mm(@NotNull String input) {
		return MINI_MESSAGE.deserialize(input);
	}

	public static boolean requirePermission(@NotNull CommandSender sender, @NotNull String permission, @NotNull String noPermissionMessage) {
		if (permission.isBlank() || sender.hasPermission(permission)) return true;
		sender.sendMessage(mm(noPermissionMessage));
		return false;
	}

	public static Player requirePlayer(@NotNull CommandSender sender, @NotNull String playerOnlyMessage) {
		if (sender instanceof Player player) return player;
		sender.sendMessage(mm(playerOnlyMessage));
		return null;
	}

	public static @NotNull List<String> filterPrefix(@NotNull Collection<String> values, @NotNull String prefix) {
		String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
		List<String> result = new ArrayList<>();
		for (String value : values) {
			if (value.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) result.add(value);
		}
		return result;
	}
}
