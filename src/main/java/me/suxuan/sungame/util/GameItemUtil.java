package me.suxuan.sungame.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * 功能物品标记工具。
 * <p>
 * 使用统一的 sungamecore:action_key 写入 PDC，方便小游戏通过配置创建和识别特殊物品。
 */
public final class GameItemUtil {
	public static final NamespacedKey ACTION_KEY = new NamespacedKey("sungamecore", "action_key");

	private GameItemUtil() {}

	public static void setActionKey(@NotNull ItemStack item, @NotNull String actionKey) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		meta.getPersistentDataContainer().set(ACTION_KEY, PersistentDataType.STRING, actionKey);
		item.setItemMeta(meta);
	}

	public static String actionKey(ItemStack item) {
		if (item == null || item.getType().isAir() || !item.hasItemMeta()) return null;
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return null;
		PersistentDataContainer container = meta.getPersistentDataContainer();
		return container.get(ACTION_KEY, PersistentDataType.STRING);
	}

	public static boolean isActionItem(ItemStack item, @NotNull String actionKey) {
		return actionKey.equals(actionKey(item));
	}
}
