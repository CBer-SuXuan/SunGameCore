package me.suxuan.sungame.api.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 子命令执行上下文。
 */
public record MiniCommandContext(
		@NotNull CommandSender sender,
		@NotNull Command command,
		@NotNull String label,
		@NotNull String subCommand,
		@NotNull String[] args
) {
	public boolean isPlayer() {
		return sender instanceof Player;
	}

	public Player playerOrNull() {
		return sender instanceof Player player ? player : null;
	}

	public void reply(@NotNull String miniMessage) {
		sender.sendMessage(CommandUtil.mm(miniMessage));
	}

	public void reply(@NotNull Component component) {
		sender.sendMessage(component);
	}
}
