package me.suxuan.sungame.api.loot;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 通用战利品表，可用于填充箱子或其他容器。
 */
public final class LootTable {
	private final int minItemsPerContainer;
	private final int maxItemsPerContainer;
	private final List<LootEntry> entries;
	private final int totalWeight;

	private LootTable(int minItemsPerContainer, int maxItemsPerContainer, @NotNull List<LootEntry> entries) {
		this.minItemsPerContainer = Math.max(1, minItemsPerContainer);
		this.maxItemsPerContainer = Math.max(this.minItemsPerContainer, maxItemsPerContainer);
		this.entries = List.copyOf(entries);
		this.totalWeight = this.entries.stream().mapToInt(LootEntry::weight).sum();
	}

	public static @NotNull LootTable fromSection(ConfigurationSection section) {
		if (section == null) return new LootTable(0, 0, List.of());
		int min = section.getInt("min-items-per-chest", section.getInt("min-items-per-container", 4));
		int max = section.getInt("max-items-per-chest", section.getInt("max-items-per-container", 8));
		List<LootEntry> entries = new ArrayList<>();
		ConfigurationSection entriesSection = section.getConfigurationSection("entries");
		if (entriesSection != null) {
			for (String key : entriesSection.getKeys(false)) {
				ConfigurationSection entrySection = entriesSection.getConfigurationSection(key);
				if (entrySection != null) entries.add(LootEntry.fromSection(entrySection));
			}
		}
		return new LootTable(min, max, entries);
	}

	public boolean empty() {
		return entries.isEmpty() || totalWeight <= 0;
	}

	public void fill(@NotNull Inventory inventory, @NotNull Random random) {
		inventory.clear();
		if (empty()) return;
		int itemCount = minItemsPerContainer == maxItemsPerContainer
				? minItemsPerContainer
				: minItemsPerContainer + random.nextInt(maxItemsPerContainer - minItemsPerContainer + 1);
		for (int i = 0; i < itemCount; i++) {
			LootEntry entry = roll(random);
			if (entry == null) continue;
			ItemStack item = entry.create(random);
			int slot = random.nextInt(inventory.getSize());
			for (int attempts = 0; attempts < inventory.getSize() && inventory.getItem(slot) != null; attempts++) {
				slot = (slot + 1) % inventory.getSize();
			}
			if (inventory.getItem(slot) == null) inventory.setItem(slot, item);
		}
	}

	public LootEntry roll(@NotNull Random random) {
		if (empty()) return null;
		int value = random.nextInt(totalWeight);
		for (LootEntry entry : entries) {
			value -= entry.weight();
			if (value < 0) return entry;
		}
		return entries.getFirst();
	}

	public int minItemsPerContainer() { return minItemsPerContainer; }
	public int maxItemsPerContainer() { return maxItemsPerContainer; }
	public @NotNull List<LootEntry> entries() { return entries; }
}
