package me.suxuan.sungame.api.bossbar;

import me.suxuan.sungame.api.queue.QueueArena;
import me.suxuan.sungame.api.session.GameSession;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 基于 Adventure BossBar 的 Bukkit 实现。
 */
public final class BukkitGameBossBarService<G extends GameSession> implements GameBossBarService<G> {
	private final Map<UUID, BossBar> bars = new HashMap<>();

	@Override
	public void show(@NotNull Player player, @NotNull Component name, float progress, @NotNull BossBar.Color color) {
		show(player, name, progress, color, BossBar.Overlay.PROGRESS);
	}

	@Override
	public void show(@NotNull Player player, @NotNull Component name, float progress, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay) {
		BossBar bar = bars.get(player.getUniqueId());
		float clampedProgress = clampProgress(progress);
		if (bar == null) {
			bar = BossBar.bossBar(name, clampedProgress, color, overlay);
			bars.put(player.getUniqueId(), bar);
			player.showBossBar(bar);
			return;
		}
		bar.name(name);
		bar.progress(clampedProgress);
		bar.color(color);
		bar.overlay(overlay);
	}

	@Override
	public void showQueue(@NotNull QueueArena queue, @NotNull Component name, float progress, @NotNull BossBar.Color color) {
		showQueue(queue, name, progress, color, BossBar.Overlay.PROGRESS);
	}

	@Override
	public void showQueue(@NotNull QueueArena queue, @NotNull Component name, float progress, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay) {
		for (UUID uuid : queue.players()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) show(player, name, progress, color, overlay);
		}
	}

	@Override
	public void showGame(@NotNull G game, @NotNull Component name, float progress, @NotNull BossBar.Color color) {
		showGame(game, name, progress, color, BossBar.Overlay.PROGRESS);
	}

	@Override
	public void showGame(@NotNull G game, @NotNull Component name, float progress, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay) {
		for (UUID uuid : game.players()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) show(player, name, progress, color, overlay);
		}
	}

	@Override
	public void clear(@NotNull Player player) {
		BossBar bar = bars.remove(player.getUniqueId());
		if (bar != null) player.hideBossBar(bar);
	}

	@Override
	public void clear(@NotNull UUID uuid) {
		BossBar bar = bars.remove(uuid);
		if (bar == null) return;
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) player.hideBossBar(bar);
	}

	@Override
	public void clearQueue(@NotNull QueueArena queue) {
		for (UUID uuid : queue.players()) clear(uuid);
	}

	@Override
	public void clearGame(@NotNull G game) {
		for (UUID uuid : game.players()) clear(uuid);
	}

	@Override
	public void clearAll() {
		for (Map.Entry<UUID, BossBar> entry : new HashMap<>(bars).entrySet()) {
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null) player.hideBossBar(entry.getValue());
		}
		bars.clear();
	}

	private float clampProgress(float progress) {
		return Math.max(0.0F, Math.min(1.0F, progress));
	}
}
