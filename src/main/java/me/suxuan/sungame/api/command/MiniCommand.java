package me.suxuan.sungame.api.command;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 通用子命令定义。
 */
public interface MiniCommand {
	@NotNull String name();
	default @NotNull List<String> aliases() { return List.of(); }
	default @NotNull String permission() { return ""; }
	default boolean playerOnly() { return false; }
	void execute(@NotNull MiniCommandContext context);
	default @NotNull List<String> tabComplete(@NotNull MiniCommandContext context) { return List.of(); }

	static @NotNull Builder builder(@NotNull String name) {
		return new Builder(name);
	}

	@FunctionalInterface
	interface Executor {
		void execute(@NotNull MiniCommandContext context);
	}

	@FunctionalInterface
	interface Completer {
		@NotNull List<String> complete(@NotNull MiniCommandContext context);
	}

	final class Builder {
		private final String name;
		private final List<String> aliases = new ArrayList<>();
		private String permission = "";
		private boolean playerOnly;
		private Executor executor = context -> {};
		private Completer completer = context -> List.of();

		private Builder(@NotNull String name) {
			this.name = Objects.requireNonNull(name, "name");
			if (name.isBlank()) throw new IllegalArgumentException("命令名不能为空");
		}

		public @NotNull Builder aliases(@NotNull String... aliases) {
			this.aliases.addAll(List.of(aliases));
			return this;
		}

		public @NotNull Builder permission(@NotNull String permission) {
			this.permission = permission;
			return this;
		}

		public @NotNull Builder playerOnly(boolean playerOnly) {
			this.playerOnly = playerOnly;
			return this;
		}

		public @NotNull Builder executor(@NotNull Executor executor) {
			this.executor = executor;
			return this;
		}

		public @NotNull Builder completer(@NotNull Completer completer) {
			this.completer = completer;
			return this;
		}

		public @NotNull MiniCommand build() {
			return new MiniCommand() {
				@Override public @NotNull String name() { return name; }
				@Override public @NotNull List<String> aliases() { return List.copyOf(aliases); }
				@Override public @NotNull String permission() { return permission; }
				@Override public boolean playerOnly() { return playerOnly; }
				@Override public void execute(@NotNull MiniCommandContext context) { executor.execute(context); }
				@Override public @NotNull List<String> tabComplete(@NotNull MiniCommandContext context) { return completer.complete(context); }
			};
		}
	}
}
