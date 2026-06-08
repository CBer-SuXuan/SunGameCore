package me.suxuan.sungame.api.session;

import me.suxuan.sungame.api.queue.QueueArena;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ManagedPlayerProvider<G extends GameSession> {
	@NotNull JavaPlugin plugin();
	@NotNull Optional<G> gameOf(@NotNull Player player);
	@NotNull Optional<G> gameByWorld(@NotNull World world);
	@NotNull Optional<QueueArena> queueOf(@NotNull Player player);

	default boolean isManaged(@NotNull Player player) {
		return gameOf(player).isPresent() || queueOf(player).isPresent();
	}

	default boolean isEliminated(@NotNull Player player) {
		return gameOf(player).map(game -> !game.alivePlayers().contains(player.getUniqueId())).orElse(false);
	}
}
