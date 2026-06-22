package me.suxuan.sungame.api.listener;

import me.suxuan.sungame.api.session.GameSession;
import me.suxuan.sungame.api.session.ManagedPlayerProvider;
import me.suxuan.sungame.util.PlayerStateUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class CommonLifecycleListener<G extends GameSession> implements Listener {
	private final ManagedPlayerProvider<G> provider;
	private final LifecycleCallbacks<G> callbacks;
	private final ProtectionPolicy protectionPolicy;
	private final LifecyclePolicy<G> lifecyclePolicy;

	public CommonLifecycleListener(@NotNull ManagedPlayerProvider<G> provider, @NotNull LifecycleCallbacks<G> callbacks,
	                               @NotNull ProtectionPolicy protectionPolicy, @NotNull LifecyclePolicy<G> lifecyclePolicy) {
		this.provider = provider;
		this.callbacks = callbacks;
		this.protectionPolicy = protectionPolicy;
		this.lifecyclePolicy = lifecyclePolicy;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (lifecyclePolicy.hideJoinMessage(player)) event.joinMessage(null);
		if (lifecyclePolicy.clearInventoryOnJoin(player)) player.getInventory().clear();
		if (lifecyclePolicy.resetPlayerOnJoin(player)) PlayerStateUtil.reset(player);
		if (callbacks.handleJoin(player)) return;
		if (callbacks.autoJoinQueue(player)) callbacks.joinQueue(player);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (!provider.isManaged(player)) return;
		if (!lifecyclePolicy.cancelFoodChange(player)) return;
		event.setCancelled(true);
		player.setFoodLevel(lifecyclePolicy.foodLevel(player));
		player.setSaturation(lifecyclePolicy.saturation(player));
	}

	@EventHandler
	public void onRegainHealth(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (provider.gameOf(player).isEmpty()) return;
		if (lifecyclePolicy.cancelNaturalRegain(player, event.getRegainReason())) event.setCancelled(true);
	}

	@EventHandler public void onDropItem(PlayerDropItemEvent event) { if (provider.isManaged(event.getPlayer()) && protectionPolicy.cancelItemDrop(event.getPlayer())) event.setCancelled(true); }
	@EventHandler public void onPickupArrow(PlayerPickupArrowEvent event) { if (provider.isManaged(event.getPlayer()) && protectionPolicy.cancelItemPickup(event.getPlayer())) event.setCancelled(true); }
	@EventHandler public void onInteractEntity(PlayerInteractEntityEvent event) { if (provider.isManaged(event.getPlayer()) && protectionPolicy.cancelEntityInteract(event.getPlayer())) event.setCancelled(true); }
	@EventHandler public void onSwapHandItems(PlayerSwapHandItemsEvent event) { if (provider.isManaged(event.getPlayer()) && protectionPolicy.cancelSwapHandItems(event.getPlayer())) event.setCancelled(true); }

	@EventHandler
	public void onPortal(PlayerPortalEvent event) {
		Player player = event.getPlayer();
		if (provider.isManaged(player) && lifecyclePolicy.cancelPortal(player)) event.setCancelled(true);
	}

	@EventHandler
	public void onPickupItem(EntityPickupItemEvent event) {
		if (event.getEntity() instanceof Player player && provider.isManaged(player) && protectionPolicy.cancelItemPickup(player)) event.setCancelled(true);
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (!provider.isManaged(player)) return;
		if (lifecyclePolicy.cancelTeleport(player, event.getCause())) event.setCancelled(true);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getPlayer();
		Optional<G> optional = provider.gameOf(player);
		if (optional.isEmpty() || !optional.get().world().equals(player.getWorld())) return;
		G game = optional.get();
		if (lifecyclePolicy.clearDeathDrops(player, game)) event.getDrops().clear();
		if (lifecyclePolicy.clearDroppedExp(player, game)) event.setDroppedExp(0);
		if (lifecyclePolicy.hideDeathMessage(player, game)) event.deathMessage(null);
		callbacks.eliminate(player, lifecyclePolicy.deathEliminateReason(player, game));
		if (lifecyclePolicy.autoRespawn(player, game)) {
			Bukkit.getScheduler().runTaskLater(provider.plugin(), () -> {
				if (player.isOnline() && player.isDead()) player.spigot().respawn();
			}, Math.max(0L, lifecyclePolicy.autoRespawnDelayTicks(player, game)));
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		Optional<G> optional = provider.gameOf(player);
		if (optional.isEmpty()) return;
		G game = optional.get();
		Location respawn = callbacks.respawnLocation(player, game);
		if (respawn != null) event.setRespawnLocation(respawn);
		Bukkit.getScheduler().runTask(provider.plugin(), () -> {
			if (!player.isOnline()) return;
			if (lifecyclePolicy.prepareSpectatorLikeOnRespawn(player, game)) PlayerStateUtil.prepareSpectatorLike(player);
			callbacks.afterRespawn(player, game);
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (lifecyclePolicy.hideQuitMessage(player)) event.quitMessage(null);
		callbacks.handleQuit(player);
	}
}
