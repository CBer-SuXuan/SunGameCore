package me.suxuan.sungame.api.loot;

import me.suxuan.sungame.util.config.ItemStackConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

/**
 * 单个战利品条目，包含物品模板、随机数量范围和权重。
 */
public record LootEntry(@NotNull ItemStack item, int minAmount, int maxAmount, int weight) {
	public LootEntry {
		Objects.requireNonNull(item, "item");
		minAmount = Math.max(1, minAmount);
		maxAmount = Math.max(minAmount, maxAmount);
		weight = Math.max(1, weight);
		item = item.clone();
	}

	public static @NotNull LootEntry fromSection(@NotNull ConfigurationSection section) {
		ItemStack item = ItemStackConfigUtil.read(section);
		return new LootEntry(item, section.getInt("min-amount", 1), section.getInt("max-amount", 1), section.getInt("weight", 1));
	}

	public @NotNull ItemStack create(@NotNull Random random) {
		int amount = minAmount == maxAmount ? minAmount : minAmount + random.nextInt(maxAmount - minAmount + 1);
		ItemStack result = item.clone();
		result.setAmount(Math.min(result.getMaxStackSize(), amount));
		return result;
	}
}
