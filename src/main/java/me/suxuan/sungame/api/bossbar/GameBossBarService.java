package me.suxuan.sungame.api.bossbar;

import me.suxuan.sungame.api.queue.QueueArena;
import me.suxuan.sungame.api.session.GameSession;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 通用 BossBar 服务。
 * <p>
 * 默认以玩家为单位维护一个 BossBar，重复显示时会复用并更新已有 BossBar。
 */
public interface GameBossBarService<G extends GameSession> {

	void show(@NotNull Player player, @NotNull Component name, float progress, @NotNull BossBar.Color color);

	void show(@NotNull Player player, @NotNull Component name, float progress, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay);

	void showQueue(@NotNull QueueArena queue, @NotNull Component name, float progress, @NotNull BossBar.Color color);

	void showQueue(@NotNull QueueArena queue, @NotNull Component name, float progress, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay);

	void showGame(@NotNull G game, @NotNull Component name, float progress, @NotNull BossBar.Color color);

	void showGame(@NotNull G game, @NotNull Component name, float progress, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay);

	void clear(@NotNull Player player);

	void clear(@NotNull UUID uuid);

	void clearQueue(@NotNull QueueArena queue);

	void clearGame(@NotNull G game);

	void clearAll();
}
