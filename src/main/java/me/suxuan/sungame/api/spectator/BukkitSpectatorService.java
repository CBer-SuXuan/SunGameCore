package me.suxuan.sungame.api.spectator;

import me.suxuan.sungame.api.session.GameSession;
import me.suxuan.sungame.util.PlayerStateUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Bukkit 实现的通用旁观者服务。
 */
public final class BukkitSpectatorService<G extends GameSession> implements SpectatorService<G> {
	private final JavaPlugin plugin;
	private final Map<UUID, String> spectatorGames = new HashMap<>();

	public BukkitSpectatorService(@NotNull JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void makeSpectator(@NotNull Player player, @NotNull G game, @NotNull SpectatorOptions options) {
		spectatorGames.put(player.getUniqueId(), game.id());
		PlayerStateUtil.prepareSpectatorLike(player);
		ItemStack leaveItem = options.leaveItem();
		if (leaveItem != null) player.getInventory().setItem(8, leaveItem);
		if (options.hideFromAlive()) hideFromAlive(player, game);
		Location spectatorSpawn = options.spectatorSpawn();
		if (spectatorSpawn != null) {
			player.teleportAsync(spectatorSpawn);
		}
	}

	@Override
	public void hideFromAlive(@NotNull Player spectator, @NotNull G game) {
		for (UUID uuid : game.alivePlayers()) {
			Player alive = Bukkit.getPlayer(uuid);
			if (alive != null && !alive.equals(spectator)) alive.hidePlayer(plugin, spectator);
		}
	}

	@Override
	public void showToGame(@NotNull Player target, @NotNull G game) {
		for (UUID uuid : game.players()) {
			Player viewer = Bukkit.getPlayer(uuid);
			if (viewer != null && !viewer.equals(target)) viewer.showPlayer(plugin, target);
		}
	}

	@Override
	public void showAll(@NotNull G game) {
		for (UUID viewerId : game.players()) {
			Player viewer = Bukkit.getPlayer(viewerId);
			if (viewer == null) continue;
			for (UUID targetId : game.players()) {
				Player target = Bukkit.getPlayer(targetId);
				if (target != null && !target.equals(viewer)) viewer.showPlayer(plugin, target);
			}
		}
	}

	@Override
	public void clear(@NotNull Player player) {
		spectatorGames.remove(player.getUniqueId());
		for (Player viewer : Bukkit.getOnlinePlayers()) {
			if (!viewer.equals(player)) viewer.showPlayer(plugin, player);
		}
	}

	@Override
	public void clearAll(@NotNull G game) {
		Set<UUID> gamePlayers = new HashSet<>(game.players());
		for (UUID uuid : gamePlayers) spectatorGames.remove(uuid);
		showAll(game);
	}
}
