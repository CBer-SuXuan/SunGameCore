package me.suxuan.sungame.api.boundary;

import me.suxuan.sungame.api.session.GameSession;
import me.suxuan.sungame.api.task.GameTaskRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * 基于 {@link GameTaskRegistry} 的 Bukkit 边界检测器。
 */
public final class BukkitBoundaryWatcher<G extends GameSession> implements BoundaryWatcher<G> {
	public static final String TASK_NAME = "boundary";

	private final GameTaskRegistry taskRegistry;

	public BukkitBoundaryWatcher(@NotNull GameTaskRegistry taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	@Override
	public void watch(@NotNull G game, @NotNull Collection<BoundaryRule<G>> rules, @NotNull BoundaryAction<G> action, long delayTicks, long periodTicks) {
		Collection<BoundaryRule<G>> safeRules = ListCopy.copyOf(rules);
		taskRegistry.repeat(game.id(), TASK_NAME, delayTicks, periodTicks, () -> tick(game, safeRules, action));
	}

	private void tick(@NotNull G game, @NotNull Collection<BoundaryRule<G>> rules, @NotNull BoundaryAction<G> action) {
		for (UUID uuid : new ArrayList<>(game.alivePlayers())) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null || !player.isOnline()) continue;
			for (BoundaryRule<G> rule : rules) {
				Optional<String> reason = rule.check(player, game);
				if (reason.isEmpty()) continue;
				action.handle(player, game, reason.get());
				break;
			}
		}
	}

	@Override
	public void stop(@NotNull G game) {
		taskRegistry.cancel(game.id(), TASK_NAME);
	}

	@Override
	public void stopAll() {
		taskRegistry.cancelAll();
	}

	private static final class ListCopy {
		private static <T> Collection<T> copyOf(Collection<T> source) {
			return java.util.List.copyOf(source);
		}
	}
}
