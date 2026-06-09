package me.suxuan.sungame.api.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 通用子命令分发器。
 */
public final class MiniCommandExecutor implements CommandExecutor, TabCompleter {
	private final Map<String, MiniCommand> commands;
	private final List<String> primaryNames;
	private final String usage;
	private final String unknownMessage;
	private final String noPermissionMessage;
	private final String playerOnlyMessage;

	private MiniCommandExecutor(@NotNull Builder builder) {
		this.commands = Map.copyOf(builder.commands);
		this.primaryNames = List.copyOf(builder.primaryNames);
		this.usage = builder.usage;
		this.unknownMessage = builder.unknownMessage;
		this.noPermissionMessage = builder.noPermissionMessage;
		this.playerOnlyMessage = builder.playerOnlyMessage;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 0) {
			sender.sendMessage(CommandUtil.mm(usage.replace("<label>", label)));
			return true;
		}
		String subCommand = args[0].toLowerCase(Locale.ROOT);
		MiniCommand miniCommand = commands.get(subCommand);
		if (miniCommand == null) {
			sender.sendMessage(CommandUtil.mm(unknownMessage));
			return true;
		}
		if (!miniCommand.permission().isBlank() && !CommandUtil.requirePermission(sender, miniCommand.permission(), noPermissionMessage)) return true;
		if (miniCommand.playerOnly() && CommandUtil.requirePlayer(sender, playerOnlyMessage) == null) return true;
		MiniCommandContext context = new MiniCommandContext(sender, command, label, subCommand, Arrays.copyOfRange(args, 1, args.length));
		miniCommand.execute(context);
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1) {
			List<String> available = new ArrayList<>();
			for (String name : primaryNames) {
				MiniCommand miniCommand = commands.get(name);
				if (miniCommand == null) continue;
				if (!miniCommand.permission().isBlank() && !sender.hasPermission(miniCommand.permission())) continue;
				available.add(name);
			}
			return CommandUtil.filterPrefix(available, args[0]);
		}
		MiniCommand miniCommand = commands.get(args[0].toLowerCase(Locale.ROOT));
		if (miniCommand == null) return List.of();
		if (!miniCommand.permission().isBlank() && !sender.hasPermission(miniCommand.permission())) return List.of();
		MiniCommandContext context = new MiniCommandContext(sender, command, label, args[0].toLowerCase(Locale.ROOT), Arrays.copyOfRange(args, 1, args.length));
		return miniCommand.tabComplete(context);
	}

	public static @NotNull Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final Map<String, MiniCommand> commands = new LinkedHashMap<>();
		private final List<String> primaryNames = new ArrayList<>();
		private String usage = "<yellow>用法: /<label> <subcommand>";
		private String unknownMessage = "<red>未知子命令";
		private String noPermissionMessage = "<red>你没有权限";
		private String playerOnlyMessage = "<red>只有玩家可以执行该命令";

		private Builder() {}

		public @NotNull Builder usage(@NotNull String usage) {
			this.usage = usage;
			return this;
		}

		public @NotNull Builder unknownMessage(@NotNull String unknownMessage) {
			this.unknownMessage = unknownMessage;
			return this;
		}

		public @NotNull Builder noPermissionMessage(@NotNull String noPermissionMessage) {
			this.noPermissionMessage = noPermissionMessage;
			return this;
		}

		public @NotNull Builder playerOnlyMessage(@NotNull String playerOnlyMessage) {
			this.playerOnlyMessage = playerOnlyMessage;
			return this;
		}

		public @NotNull Builder register(@NotNull MiniCommand command) {
			String name = command.name().toLowerCase(Locale.ROOT);
			commands.put(name, command);
			primaryNames.add(name);
			for (String alias : command.aliases()) {
				if (!alias.isBlank()) commands.put(alias.toLowerCase(Locale.ROOT), command);
			}
			return this;
		}

		public @NotNull MiniCommandExecutor build() {
			return new MiniCommandExecutor(this);
		}
	}
}
