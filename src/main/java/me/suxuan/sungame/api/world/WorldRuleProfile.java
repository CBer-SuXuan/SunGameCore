package me.suxuan.sungame.api.world;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 显式世界规则配置。
 * <p>
 * SunGameCore 不提供默认世界规则；使用库的插件必须传入一个 WorldRuleProfile。
 * 如果确实不希望修改任何规则，也应显式传入 {@link #empty()}。
 */
public final class WorldRuleProfile {
	private final Map<GameRule<?>, Object> rules = new LinkedHashMap<>();

	private WorldRuleProfile() {}

	public static @NotNull WorldRuleProfile empty() {
		return new WorldRuleProfile();
	}

	public <T> @NotNull WorldRuleProfile set(@NotNull GameRule<T> rule, @NotNull T value) {
		rules.put(Objects.requireNonNull(rule, "rule"), Objects.requireNonNull(value, "value"));
		return this;
	}

	public boolean isEmpty() {
		return rules.isEmpty();
	}

	public @NotNull Map<GameRule<?>, Object> rules() {
		return Map.copyOf(rules);
	}

	public void apply(@NotNull World world) {
		Objects.requireNonNull(world, "world");
		for (Map.Entry<GameRule<?>, Object> entry : rules.entrySet()) {
			applyRule(world, entry.getKey(), entry.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void applyRule(@NotNull World world, @NotNull GameRule<?> rule, @NotNull Object value) {
		world.setGameRule((GameRule<T>) rule, (T) value);
	}
}
