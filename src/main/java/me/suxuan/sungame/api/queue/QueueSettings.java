package me.suxuan.sungame.api.queue;

import me.suxuan.sungame.api.world.WorldRuleProfile;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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
		int quickCountdownPercent,
		@NotNull WorldRuleProfile worldRules
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
		worldRules = Objects.requireNonNull(worldRules, "QueueSettings worldRules 不能为空，必须显式传入 WorldRuleProfile.empty() 或自定义规则配置");
	}

	public int quickStartPlayers() {
		return Math.max(minPlayers, (int) Math.ceil(maxPlayers * (quickCountdownPercent / 100.0D)));
	}

	@Override
	public Location spawn() {
		return spawn.clone();
	}
}
