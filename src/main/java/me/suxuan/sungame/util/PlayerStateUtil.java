package me.suxuan.sungame.util;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public final class PlayerStateUtil {
	private PlayerStateUtil() {}

	public static void reset(@NotNull Player player, Material... cooldownMaterials) {
		player.setAllowFlight(false);
		player.setFlying(false);
		player.setInvulnerable(false);
		player.setCollidable(true);
		player.setGlowing(false);
		player.setFireTicks(0);
		player.setFreezeTicks(0);
		player.setFoodLevel(20);
		player.setSaturation(20.0F);
		player.setExhaustion(0.0F);
		player.setExp(0.0F);
		player.setLevel(0);
		player.setFallDistance(0.0F);
		player.setArrowsInBody(0);
		for (Material material : cooldownMaterials) {
			if (material != null) player.setCooldown(material, 0);
		}
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
	}

	public static void healToMax(@NotNull Player player) {
		double maxHealth = player.getAttribute(Attribute.MAX_HEALTH) == null ? 20.0D : player.getAttribute(Attribute.MAX_HEALTH).getValue();
		player.setHealth(Math.min(maxHealth, player.getHealthScale() > 0.0D ? maxHealth : 20.0D));
	}

	public static void prepareAdventure(@NotNull Player player, Material... cooldownMaterials) {
		player.getInventory().clear();
		reset(player, cooldownMaterials);
		healToMax(player);
		player.setGameMode(GameMode.ADVENTURE);
	}

	public static void prepareSurvival(@NotNull Player player, Material... cooldownMaterials) {
		player.getInventory().clear();
		reset(player, cooldownMaterials);
		healToMax(player);
		player.setGameMode(GameMode.SURVIVAL);
		player.setFoodLevel(20);
		player.setSaturation(20.0F);
	}

	public static void prepareSpectatorLike(@NotNull Player player, Material... cooldownMaterials) {
		player.getInventory().clear();
		reset(player, cooldownMaterials);
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setInvulnerable(true);
		player.setCollidable(false);
	}
}
