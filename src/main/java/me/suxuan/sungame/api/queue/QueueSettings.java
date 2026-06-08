package me.suxuan.sungame.api.queue;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * 单个小游戏独立的 queue 配置。
 */
public record QueueSettings(
		@NotNull String idPrefix,
		@NotNull String templateWorld,
		@NotNull Location spawn,
		int minPlayers,
		int maxPlayers,
		int longCountdownSeconds,
		int quickCountdownSeconds,
		int quickCountdownPercent
) {
	public QueueSettings {
		if (idPrefix.isBlank()) throw new IllegalArgumentException("队列 idPrefix 不能为空");
		if (templateWorld.isBlank()) throw new IllegalArgumentException("队列 templateWorld 不能为空");
		if (minPlayers < 1) minPlayers = 1;
		if (maxPlayers < 1) maxPlayers = 1;
		if (minPlayers > maxPlayers) minPlayers = maxPlayers;
		if (longCountdownSeconds < 1) longCountdownSeconds = 1;
		if (quickCountdownSeconds < 1) quickCountdownSeconds = 1;
		quickCountdownPercent = Math.clamp(quickCountdownPercent, 1, 100);
		spawn = spawn.clone();
	}

	public int quickStartPlayers() {
		return Math.max(minPlayers, (int) Math.ceil(maxPlayers * (quickCountdownPercent / 100.0D)));
	}

	@Override
	public Location spawn() {
		return spawn.clone();
	}
}
