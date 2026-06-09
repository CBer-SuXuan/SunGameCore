package me.suxuan.sungame.api.loot;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 根据与中心点的平面距离选择 loot tier。
 */
public final class DistanceLootSelector {
	private final double centerX;
	private final double centerZ;
	private final List<LootTierRule> rules;
	private final String fallbackTier;
	private final LootRegistry lootRegistry;

	private DistanceLootSelector(double centerX, double centerZ, @NotNull List<LootTierRule> rules, @NotNull String fallbackTier, @NotNull LootRegistry lootRegistry) {
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.rules = List.copyOf(rules);
		this.fallbackTier = fallbackTier;
		this.lootRegistry = lootRegistry;
	}

	public static @NotNull DistanceLootSelector fromSection(ConfigurationSection section, @NotNull Location defaultCenter, @NotNull LootRegistry globalLootRegistry) {
		LootRegistry mapLootRegistry = globalLootRegistry.mergedWith(section);
		double centerX = defaultCenter.getX();
		double centerZ = defaultCenter.getZ();
		List<LootTierRule> rules = new ArrayList<>();
		if (section != null) {
			ConfigurationSection centerSection = section.getConfigurationSection("center");
			if (centerSection != null) {
				centerX = centerSection.getDouble("x", centerX);
				centerZ = centerSection.getDouble("z", centerZ);
			}
			for (Map<?, ?> raw : section.getMapList("tiers")) {
				rules.add(LootTierRule.fromMap(raw));
			}
			ConfigurationSection tiersSection = section.getConfigurationSection("tiers");
			if (rules.isEmpty() && tiersSection != null) {
				for (String key : tiersSection.getKeys(false)) {
					ConfigurationSection ruleSection = tiersSection.getConfigurationSection(key);
					if (ruleSection != null) rules.add(LootTierRule.fromSection(ruleSection));
				}
			}
		}
		String fallbackTier = mapLootRegistry.defaultTier();
		if (rules.isEmpty()) rules.add(new LootTierRule(fallbackTier, -1.0D));
		rules.sort(Comparator.comparingDouble(rule -> rule.maxDistance() < 0.0D ? Double.MAX_VALUE : rule.maxDistance()));
		return new DistanceLootSelector(centerX, centerZ, rules, fallbackTier, mapLootRegistry);
	}

	public @NotNull String tierFor(@NotNull Location location) {
		double dx = location.getX() - centerX;
		double dz = location.getZ() - centerZ;
		double distance = Math.sqrt(dx * dx + dz * dz);
		for (LootTierRule rule : rules) {
			if (rule.matches(distance)) return rule.id();
		}
		return fallbackTier;
	}

	public @NotNull LootRegistry lootRegistry() { return lootRegistry; }
	public double centerX() { return centerX; }
	public double centerZ() { return centerZ; }
	public @NotNull List<LootTierRule> rules() { return rules; }
}
