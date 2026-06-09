package me.suxuan.sungame.api.loot;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 根据距离匹配 loot tier 的规则。
 */
public record LootTierRule(@NotNull String id, double maxDistance) {
	public boolean matches(double distance) {
		return maxDistance < 0.0D || distance <= maxDistance;
	}

	public static @NotNull LootTierRule fromSection(@NotNull ConfigurationSection section) {
		return new LootTierRule(section.getString("id", "normal"), section.getDouble("max-distance", -1.0D));
	}

	public static @NotNull LootTierRule fromMap(@NotNull Map<?, ?> map) {
		Object idValue = map.get("id");
		String id = idValue == null ? "normal" : idValue.toString();
		Object distanceValue = map.get("max-distance");
		double maxDistance = distanceValue instanceof Number number ? number.doubleValue() : -1.0D;
		return new LootTierRule(id, maxDistance);
	}
}
