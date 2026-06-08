package me.suxuan.sungame.api.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.suxuan.sungame.api.queue.QueueArena;
import me.suxuan.sungame.api.session.GameSession;
import me.suxuan.sungame.api.session.ManagedPlayerProvider;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 通用聊天隔离监听器。
 * 游戏内玩家只能被同一场游戏内玩家看到，queue 内玩家只能被同一 queue 内玩家看到。
 */
public final class CommonChatListener<G extends GameSession> implements Listener {
	private final ManagedPlayerProvider<G> provider;
	private final ChatPolicy<G> policy;

	public CommonChatListener(@NotNull ManagedPlayerProvider<G> provider, @NotNull ChatPolicy<G> policy) {
		this.provider = provider;
		this.policy = policy;
	}

	@EventHandler(ignoreCancelled = true)
	public void onChat(AsyncChatEvent event) {
		Player player = event.getPlayer();
		Optional<G> game = provider.gameOf(player);
		if (game.isPresent()) {
			if (policy.isolateGameChat(player, game.get())) {
				isolate(event.viewers(), game.get().players(), policy.allowConsoleSeeGameChat(player, game.get()));
			}
			return;
		}

		Optional<QueueArena> queue = provider.queueOf(player);
		if (queue.isPresent() && policy.isolateQueueChat(player, queue.get())) {
			isolate(event.viewers(), queue.get().players(), policy.allowConsoleSeeQueueChat(player, queue.get()));
		}
	}

	private void isolate(@NotNull Set<Audience> viewers, @NotNull Set<UUID> allowedPlayers, boolean keepNonPlayers) {
		Set<Player> onlineAllowedPlayers = new HashSet<>();
		for (UUID uuid : allowedPlayers) {
			Player viewer = Bukkit.getPlayer(uuid);
			if (viewer != null) onlineAllowedPlayers.add(viewer);
		}

		viewers.removeIf(viewer -> {
			if (viewer instanceof Player playerViewer) return !onlineAllowedPlayers.contains(playerViewer);
			return !keepNonPlayers;
		});
	}
}
