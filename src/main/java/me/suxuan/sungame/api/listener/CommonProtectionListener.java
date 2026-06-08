package me.suxuan.sungame.api.listener;

import me.suxuan.sungame.api.session.GameSession;
import me.suxuan.sungame.api.session.GameState;
import me.suxuan.sungame.api.session.ManagedPlayerProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class CommonProtectionListener<G extends GameSession> implements Listener {
	private final ManagedPlayerProvider<G> provider;
	private final ProtectionPolicy policy;

	public CommonProtectionListener(@NotNull ManagedPlayerProvider<G> provider, @NotNull ProtectionPolicy policy) {
		this.provider = provider;
		this.policy = policy;
	}

	@EventHandler(ignoreCancelled = true)
	public void onBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		if (provider.isManaged(player) && policy.cancelBucket(player)) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		if (provider.isManaged(player) && policy.cancelBucket(player)) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (event.getPlayer() instanceof Player player && provider.isManaged(player) && policy.cancelInventory(player)) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player player && provider.isManaged(player) && policy.cancelInventory(player)) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent event) {
		if (event.getWhoClicked() instanceof Player player && provider.isManaged(player) && policy.cancelInventory(player)) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (provider.isManaged(player) && policy.cancelBlockPlace(player)) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (provider.isManaged(player) && policy.cancelBlockBreak(player)) event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		if (provider.gameByWorld(event.getLocation().getWorld()).isPresent()) event.blockList().clear();
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent event) {
		if (provider.gameByWorld(event.getBlock().getWorld()).isPresent()) event.blockList().clear();
	}

	@EventHandler(ignoreCancelled = true)
	public void onAnyDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		Optional<G> optional = provider.gameOf(player);
		boolean shouldCancel = (provider.queueOf(player).isPresent() && policy.cancelQueueDamage(player))
				|| (provider.isEliminated(player) && policy.cancelEliminatedDamage(player))
				|| optional.map(game -> game.state() == GameState.ENDING).orElse(false);
		if (shouldCancel) {
			event.setCancelled(true);
			player.setFireTicks(0);
			player.setFallDistance(0.0F);
		}
	}
}
