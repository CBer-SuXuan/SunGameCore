package me.suxuan.sungame.util.config;

import me.suxuan.sungame.util.GameItemUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * ItemStack 配置读取工具。
 */
public final class ItemStackConfigUtil {
	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

	private ItemStackConfigUtil() {}

	public static @NotNull ItemStack read(@NotNull ConfigurationSection section) {
		return read(section, section.getInt("amount", 1));
	}

	public static @NotNull ItemStack read(@NotNull ConfigurationSection section, int amount) {
		Material material = readMaterial(section, "material");
		ItemStack item = new ItemStack(material, Math.max(1, Math.min(material.getMaxStackSize(), amount)));
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			applyDisplay(section, meta);
			applyFlags(section, meta);
			applyEnchantments(section, meta);
			applyAttributes(section, meta);
			item.setItemMeta(meta);
		}
		applyActionKey(section, item);
		return item;
	}

	public static @NotNull Material readMaterial(@NotNull ConfigurationSection section, @NotNull String path) {
		String materialName = Objects.requireNonNull(section.getString(path), "配置 " + section.getCurrentPath() + " 缺少 " + path);
		Material material = Material.matchMaterial(materialName);
		if (material == null || material.isAir()) throw new IllegalArgumentException("配置 " + section.getCurrentPath() + " 的物品材质无效: " + materialName);
		return material;
	}

	private static void applyDisplay(@NotNull ConfigurationSection section, @NotNull ItemMeta meta) {
		String name = section.getString("name");
		if (name != null) meta.displayName(MINI_MESSAGE.deserialize(name));
		List<String> lore = section.getStringList("lore");
		if (!lore.isEmpty()) meta.lore(lore.stream().map(MINI_MESSAGE::deserialize).toList());
		if (section.contains("custom-model-data")) meta.setCustomModelData(section.getInt("custom-model-data"));
		if (section.getBoolean("unbreakable", false)) meta.setUnbreakable(true);
	}

	private static void applyActionKey(@NotNull ConfigurationSection section, @NotNull ItemStack item) {
		String actionKey = section.getString("action-key");
		if (actionKey != null && !actionKey.isBlank()) GameItemUtil.setActionKey(item, actionKey);
	}

	private static void applyFlags(@NotNull ConfigurationSection section, @NotNull ItemMeta meta) {
		for (String flagName : section.getStringList("flags")) {
			try {
				meta.addItemFlags(ItemFlag.valueOf(normalizeEnumName(flagName)));
			} catch (IllegalArgumentException exception) {
				throw new IllegalArgumentException("配置 " + section.getCurrentPath() + " 的 ItemFlag 无效: " + flagName, exception);
			}
		}
	}

	private static void applyEnchantments(@NotNull ConfigurationSection section, @NotNull ItemMeta meta) {
		ConfigurationSection enchantments = section.getConfigurationSection("enchantments");
		if (enchantments == null) return;
		for (String key : enchantments.getKeys(false)) {
			Enchantment enchantment = parseEnchantment(key, section.getCurrentPath() + ".enchantments." + key);
			int level;
			boolean ignoreRestriction;
			if (enchantments.isConfigurationSection(key)) {
				ConfigurationSection enchantSection = enchantments.getConfigurationSection(key);
				level = enchantSection.getInt("level", 1);
				ignoreRestriction = enchantSection.getBoolean("ignore-level-restriction", true);
			} else {
				level = enchantments.getInt(key, 1);
				ignoreRestriction = true;
			}
			meta.addEnchant(enchantment, Math.max(1, level), ignoreRestriction);
		}
	}

	private static void applyAttributes(@NotNull ConfigurationSection section, @NotNull ItemMeta meta) {
		ConfigurationSection attributes = section.getConfigurationSection("attributes");
		if (attributes == null) return;
		for (String key : attributes.getKeys(false)) {
			if (attributes.isList(key)) continue;
			ConfigurationSection attributeSection = attributes.getConfigurationSection(key);
			if (attributeSection == null) continue;
			addAttributeModifier(meta, key, attributeSection, section.getCurrentPath() + ".attributes." + key);
		}
		List<?> list = attributes.getList("list");
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				Object value = list.get(i);
				if (value instanceof ConfigurationSection attributeSection) {
					String attributeName = Objects.requireNonNull(attributeSection.getString("attribute"), "属性配置缺少 attribute: " + section.getCurrentPath() + ".attributes.list[" + i + "]");
					addAttributeModifier(meta, attributeName, attributeSection, section.getCurrentPath() + ".attributes.list[" + i + "]");
				}
			}
		}
	}

	private static void addAttributeModifier(@NotNull ItemMeta meta, @NotNull String attributeName, @NotNull ConfigurationSection section, @NotNull String path) {
		Attribute attribute = parseAttribute(attributeName, path);
		double amount = section.getDouble("amount");
		AttributeModifier.Operation operation = parseOperation(section.getString("operation", "ADD_NUMBER"), path);
		EquipmentSlotGroup slotGroup = parseSlotGroup(section.getString("slot", "ANY"), path);
		String name = section.getString("name", "sungame_" + normalizeKey(attributeName));
		NamespacedKey key = new NamespacedKey("sungamecore", name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_/.-]", "_"));
		AttributeModifier modifier = new AttributeModifier(key, amount, operation, slotGroup);
		meta.addAttributeModifier(attribute, modifier);
	}

	private static @NotNull Enchantment parseEnchantment(@NotNull String raw, @NotNull String path) {
		String key = normalizeKey(raw);
		Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key));
		if (enchantment == null && raw.contains(":")) enchantment = Registry.ENCHANTMENT.get(NamespacedKey.fromString(raw.toLowerCase(Locale.ROOT)));
		if (enchantment == null) enchantment = Enchantment.getByName(normalizeEnumName(raw));
		if (enchantment == null) throw new IllegalArgumentException("配置 " + path + " 的附魔无效: " + raw);
		return enchantment;
	}

	private static @NotNull Attribute parseAttribute(@NotNull String raw, @NotNull String path) {
		String key = normalizeKey(raw);
		Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
		if (attribute == null && raw.contains(":")) attribute = Registry.ATTRIBUTE.get(NamespacedKey.fromString(raw.toLowerCase(Locale.ROOT)));
		if (attribute == null) throw new IllegalArgumentException("配置 " + path + " 的属性无效: " + raw);
		return attribute;
	}

	private static @NotNull AttributeModifier.Operation parseOperation(@NotNull String raw, @NotNull String path) {
		try {
			return AttributeModifier.Operation.valueOf(normalizeEnumName(raw));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("配置 " + path + " 的属性 operation 无效: " + raw, exception);
		}
	}

	private static @NotNull EquipmentSlotGroup parseSlotGroup(@NotNull String raw, @NotNull String path) {
		String normalized = normalizeEnumName(raw);
		return switch (normalized) {
			case "ANY" -> EquipmentSlotGroup.ANY;
			case "HAND", "MAIN_HAND" -> EquipmentSlotGroup.HAND;
			case "OFF_HAND" -> EquipmentSlotGroup.OFFHAND;
			case "HEAD", "HELMET" -> EquipmentSlotGroup.HEAD;
			case "CHEST", "CHESTPLATE" -> EquipmentSlotGroup.CHEST;
			case "LEGS", "LEGGINGS" -> EquipmentSlotGroup.LEGS;
			case "FEET", "BOOTS" -> EquipmentSlotGroup.FEET;
			case "ARMOR" -> EquipmentSlotGroup.ARMOR;
			default -> throw new IllegalArgumentException("配置 " + path + " 的属性 slot 无效: " + raw);
		};
	}

	private static @NotNull String normalizeEnumName(@NotNull String raw) {
		return raw.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
	}

	private static @NotNull String normalizeKey(@NotNull String raw) {
		String value = raw.trim().toLowerCase(Locale.ROOT);
		if (value.startsWith("minecraft:")) value = value.substring("minecraft:".length());
		if (value.startsWith("generic.")) value = value.substring("generic.".length());
		return value.replace(' ', '_').replace('-', '_');
	}
}
