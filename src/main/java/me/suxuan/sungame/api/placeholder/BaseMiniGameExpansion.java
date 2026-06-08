package me.suxuan.sungame.api.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.suxuan.sungame.api.queue.QueueArena;
import me.suxuan.sungame.api.session.GameSession;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI 通用小游戏占位符基类。
 * 具体游戏继承后只需要提供 identifier、author、version 和自定义占位符。
 */
public abstract class BaseMiniGameExpansion<G extends GameSession> extends PlaceholderExpansion {
	private final PlaceholderValueProvider<G> provider;

	protected BaseMiniGameExpansion(@NotNull PlaceholderValueProvider<G> provider) {
		this.provider = provider;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
		if (!(offlinePlayer instanceof Player player)) return "";
		G game = provider.gameOf(player);
		QueueArena queue = provider.queueOf(player);
		String key = params.toLowerCase();
		String common = resolveCommon(player, game, queue, key);
		if (common != null) return common;
		return provider.resolveCustom(player, game, queue, key);
	}

	private String resolveCommon(Player player, G game, QueueArena queue, String key) {
		return switch (key) {
			case "area_type" -> game != null ? "game" : queue != null ? "queue" : "none";
			case "area_id" -> game != null ? game.id() : queue != null ? queue.id() : "";
			case "map" -> game != null ? game.mapId() : queue != null ? "queue" : "";
			case "alive" -> game != null ? String.valueOf(game.alivePlayers().size()) : "0";
			case "players" -> game != null ? String.valueOf(game.players().size()) : queue != null ? String.valueOf(queue.players().size()) : "0";
			case "queue_players" -> queue != null ? String.valueOf(queue.players().size()) : "0";
			case "max_players" -> String.valueOf(provider.maxPlayers());
			case "min_players" -> String.valueOf(provider.minPlayers());
			case "eliminated" -> game != null ? String.valueOf(!game.alivePlayers().contains(player.getUniqueId())) : "false";
			default -> null;
		};
	}
}
