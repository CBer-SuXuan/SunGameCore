package me.suxuan.sungame.api.spectator;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * 玩家进入旁观状态时的选项。
 *
 * @param spectatorSpawn 旁观出生点，可为 null
 * @param leaveItem      离开物品，可为 null
 * @param hideFromAlive  是否对存活玩家隐藏该旁观者
 */
public record SpectatorOptions(
		@Nullable Location spectatorSpawn,
		@Nullable ItemStack leaveItem,
		boolean hideFromAlive
) {
	public SpectatorOptions {
		if (spectatorSpawn != null) spectatorSpawn = spectatorSpawn.clone();
		if (leaveItem != null) leaveItem = leaveItem.clone();
	}

	public static SpectatorOptions basic() {
		return new SpectatorOptions(null, null, true);
	}

	public static SpectatorOptions at(@Nullable Location spectatorSpawn) {
		return new SpectatorOptions(spectatorSpawn, null, true);
	}

	public static SpectatorOptions of(@Nullable Location spectatorSpawn, @Nullable ItemStack leaveItem) {
		return new SpectatorOptions(spectatorSpawn, leaveItem, true);
	}

	@Override
	public Location spectatorSpawn() {
		return spectatorSpawn == null ? null : spectatorSpawn.clone();
	}

	@Override
	public ItemStack leaveItem() {
		return leaveItem == null ? null : leaveItem.clone();
	}
}
