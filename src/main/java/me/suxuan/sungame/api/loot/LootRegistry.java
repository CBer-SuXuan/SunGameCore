package me.suxuan.sungame.api.loot;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多个战利品表组成的注册表，通常按 tier 管理。
 */
public final class LootRegistry {
	private final String defaultTier;
	private final Map<String, LootTable> tiers;

	private LootRegistry(@NotNull String defaultTier, @NotNull Map<String, LootTable> tiers) {
		this.defaultTier = defaultTier;
		this.tiers = Map.copyOf(tiers);
	}

	public static @NotNull LootRegistry fromSection(ConfigurationSection section) {
		Map<String, LootTable> tiers = new LinkedHashMap<>();
		String defaultTier = section == null ? "normal" : section.getString("default-tier", "normal");
		ConfigurationSection tiersSection = section == null ? null : section.getConfigurationSection("tiers");
		if (tiersSection != null) {
			readTables(tiersSection, tiers);
		} else if (section != null && section.isConfigurationSection("entries")) {
			// 兼容旧配置：loot 下直接写 min-items-per-chest / entries。
			tiers.put(defaultTier, LootTable.fromSection(section));
		}
		if (tiers.isEmpty()) tiers.put(defaultTier, LootTable.fromSection(null));
		if (!tiers.containsKey(defaultTier)) defaultTier = tiers.keySet().iterator().next();
		return new LootRegistry(defaultTier, tiers);
	}

	public @NotNull LootRegistry mergedWith(ConfigurationSection mapLootSection) {
		if (mapLootSection == null) return this;
		Map<String, LootTable> merged = new LinkedHashMap<>(tiers);
		String mergedDefaultTier = mapLootSection.getString("default-tier", defaultTier);
		ConfigurationSection tablesSection = mapLootSection.getConfigurationSection("tables");
		if (tablesSection == null) tablesSection = mapLootSection.getConfigurationSection("loot-tiers");
		if (tablesSection != null) readTables(tablesSection, merged);
		if (!merged.containsKey(mergedDefaultTier)) mergedDefaultTier = defaultTier;
		if (!merged.containsKey(mergedDefaultTier) && !merged.isEmpty()) mergedDefaultTier = merged.keySet().iterator().next();
		return new LootRegistry(mergedDefaultTier, merged);
	}

	private static void readTables(@NotNull ConfigurationSection section, @NotNull Map<String, LootTable> target) {
		for (String key : section.getKeys(false)) {
			ConfigurationSection tierSection = section.getConfigurationSection(key);
			if (tierSection != null) target.put(key, LootTable.fromSection(tierSection));
		}
	}

	public @NotNull LootTable table(@NotNull String tierId) {
		LootTable table = tiers.get(tierId);
		if (table != null) return table;
		return tiers.getOrDefault(defaultTier, tiers.values().iterator().next());
	}

	public @NotNull String defaultTier() { return defaultTier; }
	public @NotNull Map<String, LootTable> tiers() { return tiers; }
}
