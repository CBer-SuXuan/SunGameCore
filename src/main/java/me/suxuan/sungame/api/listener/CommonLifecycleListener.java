package me.suxuan.sungame.api.listener;

import me.suxuan.sungame.api.session.GameSession;
import me.suxuan.sungame.api.session.ManagedPlayerProvider;
import me.suxuan.sungame.util.PlayerStateUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
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

	public CommonLifecycleListener(@NotNull ManagedPlayerProvider<G> provider, @NotNull LifecycleCallbacks<G> callbacks) {
		this.provider = provider;
		this.callbacks = callbacks;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		event.joinMessage(null);
		Player player = event.getPlayer();
		player.getInventory().clear();
		PlayerStateUtil.reset(player);
		if (player.isOp()) {
			World mainWorld = Bukkit.getWorld("world");
			if (mainWorld == null) mainWorld = Bukkit.getWorlds().getFirst();
			player.setGameMode(GameMode.CREATIVE);
			player.teleportAsync(mainWorld.getSpawnLocation());
			return;
		}
		if (callbacks.autoJoinQueue(player)) callbacks.joinQueue(player);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (provider.isManaged(player)) {
			event.setCancelled(true);
			player.setFoodLevel(20);
			player.setSaturation(20.0F);
		}
	}

	@EventHandler
	public void onRegainHealth(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (provider.gameOf(player).isEmpty()) return;
		if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED
				|| event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
			event.setCancelled(true);
		}
	}

	@EventHandler public void onDropItem(PlayerDropItemEvent event) { if (provider.isManaged(event.getPlayer())) event.setCancelled(true); }
	@EventHandler public void onPickupArrow(PlayerPickupArrowEvent event) { if (provider.isManaged(event.getPlayer())) event.setCancelled(true); }
	@EventHandler public void onPortal(PlayerPortalEvent event) { if (provider.isManaged(event.getPlayer())) event.setCancelled(true); }
	@EventHandler public void onInteractEntity(PlayerInteractEntityEvent event) { if (provider.isManaged(event.getPlayer())) event.setCancelled(true); }
	@EventHandler public void onSwapHandItems(PlayerSwapHandItemsEvent event) { if (provider.isManaged(event.getPlayer())) event.setCancelled(true); }

	@EventHandler
	public void onPickupItem(EntityPickupItemEvent event) {
		if (event.getEntity() instanceof Player player && provider.isManaged(player)) event.setCancelled(true);
	}

	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (!provider.isManaged(player)) return;
		PlayerTeleportEvent.TeleportCause cause = event.getCause();
		if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
				|| cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
				|| cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
				|| cause == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getPlayer();
		Optional<G> optional = provider.gameOf(player);
		if (optional.isEmpty() || !optional.get().world().equals(player.getWorld())) return;
		event.getDrops().clear();
		event.setDroppedExp(0);
		event.deathMessage(null);
		callbacks.eliminate(player, "死亡");
		Bukkit.getScheduler().runTaskLater(provider.plugin(), () -> {
			if (player.isOnline() && player.isDead()) player.spigot().respawn();
		}, 2L);
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		Optional<G> optional = provider.gameOf(player);
		if (optional.isEmpty()) return;
		Location respawn = callbacks.respawnLocation(player, optional.get());
		if (respawn != null) event.setRespawnLocation(respawn);
		Bukkit.getScheduler().runTask(provider.plugin(), () -> {
			if (!player.isOnline()) return;
			PlayerStateUtil.prepareSpectatorLike(player);
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		event.quitMessage(null);
		callbacks.handleQuit(event.getPlayer());
	}
}
