package me.suxuan.sungame.api.boundary;

import me.suxuan.sungame.api.session.GameSession;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 通用边界检测器。
 */
public interface BoundaryWatcher<G extends GameSession> {
	/**
	 * 监听某局游戏的边界规则。
	 *
	 * @param game        目标游戏
	 * @param rules       按顺序检查的规则集合
	 * @param action      命中规则后的动作
	 * @param delayTicks  首次检测延迟
	 * @param periodTicks 检测间隔
	 */
	void watch(@NotNull G game, @NotNull Collection<BoundaryRule<G>> rules, @NotNull BoundaryAction<G> action, long delayTicks, long periodTicks);

	/**
	 * 停止某局游戏的边界检测。
	 */
	void stop(@NotNull G game);

	/**
	 * 停止全部边界检测。
	 */
	void stopAll();
}
