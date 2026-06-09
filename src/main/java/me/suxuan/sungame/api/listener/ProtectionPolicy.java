package me.suxuan.sungame.api.listener;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 通用保护监听器的开关策略。
 */
public interface ProtectionPolicy {
	default boolean cancelBucket(@NotNull Player player) { return true; }
	default boolean cancelInventory(@NotNull Player player) { return true; }
	default boolean cancelInventoryOpen(@NotNull Player player) { return cancelInventory(player); }
	default boolean cancelInventoryClick(@NotNull Player player) { return cancelInventory(player); }
	default boolean cancelInventoryDrag(@NotNull Player player) { return cancelInventory(player); }
	default boolean cancelBlockPlace(@NotNull Player player) { return true; }
	default boolean cancelBlockBreak(@NotNull Player player) { return true; }
	default boolean cancelItemDrop(@NotNull Player player) { return true; }
	default boolean cancelItemPickup(@NotNull Player player) { return true; }
	default boolean cancelEntityInteract(@NotNull Player player) { return true; }
	default boolean cancelSwapHandItems(@NotNull Player player) { return true; }
	default boolean cancelQueueDamage(@NotNull Player player) { return true; }
	default boolean cancelEliminatedDamage(@NotNull Player player) { return true; }
}
