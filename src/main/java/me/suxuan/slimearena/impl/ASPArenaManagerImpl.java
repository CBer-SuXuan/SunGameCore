package me.suxuan.slimearena.impl;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.loaders.file.FileLoader;
import me.suxuan.sungame.SunGameCorePlugin;
import me.suxuan.slimearena.api.ArenaManager;
import me.suxuan.sungame.api.world.WorldRuleProfile;
import me.suxuan.sungame.hook.WorldGuardHook;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ASPArenaManagerImpl implements ArenaManager {

	private final SunGameCorePlugin plugin;
	private final AdvancedSlimePaperAPI slimeAPI;
	private final SlimeLoader fileLoader;
	private final WorldGuardHook worldGuardHook;

	private final Set<String> activeArenas = ConcurrentHashMap.newKeySet();

	public ASPArenaManagerImpl(SunGameCorePlugin plugin) {
		this.plugin = plugin;
		// 获取 ASP 核心集成的 API 实例
		this.slimeAPI = AdvancedSlimePaperAPI.instance();

		if (this.slimeAPI == null) {
			throw new IllegalStateException("无法获取 SlimeWorldManager API 实例！");
		}

		// 获取本地文件加载器（对应配置中的文件系统存储）
		this.fileLoader = new FileLoader(new File(plugin.getDataFolder() + "/template"));
		this.worldGuardHook = new WorldGuardHook(plugin);
	}

	@Override
	public @NotNull CompletableFuture<World> createArenaAsync(@NotNull String templateName, @NotNull String instanceName, @NotNull WorldRuleProfile ruleProfile) {
		if (ruleProfile == null) throw new IllegalArgumentException("ruleProfile 不能为空，必须显式传入 WorldRuleProfile.empty() 或自定义规则配置");
		CompletableFuture<World> future = new CompletableFuture<>();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				SlimePropertyMap properties = new SlimePropertyMap();
				properties.setValue(SlimeProperties.ALLOW_ANIMALS, false);
				properties.setValue(SlimeProperties.ALLOW_MONSTERS, false);
				properties.setValue(SlimeProperties.DIFFICULTY, "normal");

				SlimeWorld templateWorld = slimeAPI.readWorld(fileLoader, templateName, true, properties);
				SlimeWorld gameInstance = templateWorld.clone(instanceName);

				Bukkit.getScheduler().runTask(plugin, () -> {
					try {
						slimeAPI.loadWorld(gameInstance, false); //

						World bukkitWorld = Bukkit.getWorld(instanceName); //
						if (bukkitWorld == null) { //
							throw new RuntimeException("Slime世界已生成，但无法在 Bukkit 中找到世界实例：" + instanceName); //
						}

						activeArenas.add(instanceName);
						ruleProfile.apply(bukkitWorld);
						worldGuardHook.cleanupTransientWorld(bukkitWorld);

						plugin.log("<green>成功创建小游戏世界实例: <aqua>" + instanceName + "</aqua></green>"); //

						future.complete(bukkitWorld);

					} catch (Exception e) {
						plugin.getLogger().severe("主线程生成世界失败: " + instanceName);
						e.printStackTrace();
						future.completeExceptionally(e);
					}
				});

			} catch (Exception e) {
				// 捕获异步读取文件阶段的异常
				plugin.getLogger().severe("异步读取模板失败: " + templateName);
				e.printStackTrace();
				future.completeExceptionally(e);
			}
		});

		return future;
	}

	@Override
	public @NotNull CompletableFuture<Void> discardArenaAsync(@NotNull World world, @Nullable Location fallbackLocation) {
		// 手动创建一个 Future，确保真正卸载完毕后才回调
		CompletableFuture<Void> future = new CompletableFuture<>();
		String worldName = world.getName();
		boolean managedArena = activeArenas.contains(worldName);

		// 必须在主线程处理实体和玩家的传送
		Bukkit.getScheduler().runTask(plugin, () -> {
			boolean hasPlayers = false;

			// 1. 处理世界内残留的玩家
			for (Player player : world.getPlayers()) {
				hasPlayers = true;
				if (fallbackLocation != null) {
					player.teleportAsync(fallbackLocation);
				} else {
					player.kick(Component.text("§c游戏已结束，且未配置返回地点。"));
				}
			}

			// 如果有玩家，延迟 60 Tick (3秒) 等待异步传送完全结束；如果没有玩家，延迟 1 Tick 即可
			long delayTicks = hasPlayers ? 60L : 1L;

			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				if (managedArena) worldGuardHook.cleanupTransientWorld(world);

				// 2. 执行世界卸载（false 代表：绝对不要保存）
				boolean unloaded = Bukkit.unloadWorld(world, false);

				if (unloaded) {
					activeArenas.remove(worldName); // 【修复】必须从活跃列表中移除！
					if (managedArena) worldGuardHook.cleanupTransientWorld(world, true);
					plugin.log("<gray>世界 <aqua>" + worldName + "</aqua> 已成功销毁并从内存中清理。</gray>");
					future.complete(null); // 通知外部调用者卸载已彻底完成
				} else {
					plugin.log("<red>警告：无法卸载世界 <aqua>" + worldName + "</aqua>！可能有其他插件强行占用了区块。</red>");
					future.completeExceptionally(new RuntimeException("无法卸载世界: " + worldName));
				}
			}, delayTicks);
		});

		return future;
	}

	@Override
	public boolean isArenaWorld(@NotNull World world) {
		return activeArenas.contains(world.getName());
	}

	/**
	 * 获取所有当前活跃的竞技场世界名称
	 * (用于插件卸载时的安全清理)
	 */
	public Set<String> getActiveArenas() {
		return activeArenas;
	}
}