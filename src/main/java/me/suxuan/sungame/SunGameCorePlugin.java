package me.suxuan.sungame;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import me.suxuan.slimearena.api.ArenaManager;
import me.suxuan.slimearena.impl.ASPArenaManagerImpl;
import me.suxuan.sungame.api.MiniGameService;
import me.suxuan.sungame.impl.MiniGameServiceImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class SunGameCorePlugin extends JavaPlugin {
	private static SunGameCorePlugin instance;
	private final MiniMessage miniMessage = MiniMessage.miniMessage();
	private ASPArenaManagerImpl arenaManager;
	private MiniGameServiceImpl miniGameService;

	@Override
	public void onEnable() {
		instance = this;

		if (!checkSlimePaperEnvironment()) {
			log("<red>启动失败：未检测到 AdvancedSlimePaper 核心。</red>");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.arenaManager = new ASPArenaManagerImpl(this);
		getServer().getServicesManager().register(ArenaManager.class, arenaManager, this, ServicePriority.Normal);

		this.miniGameService = new MiniGameServiceImpl(this, arenaManager);
		getServer().getServicesManager().register(MiniGameService.class, miniGameService, this, ServicePriority.Normal);

		log("<green>SunGameCore 已启用，ArenaManager 与 MiniGameService 已注册。</green>");
	}

	@Override
	public void onDisable() {
		if (miniGameService != null) {
			miniGameService.stopAllQueues();
		}

		if (arenaManager != null) {
			log("<yellow>正在清理残留临时竞技场世界...</yellow>");
			for (String arenaName : arenaManager.getActiveArenas()) {
				World world = Bukkit.getWorld(arenaName);
				if (world == null) continue;
				for (Player player : world.getPlayers()) {
					player.kick(Component.text("服务器正在关闭/重启"));
				}
				Bukkit.unloadWorld(world, false);
				log("<gray>强制清理残留世界: <aqua>" + arenaName + "</aqua></gray>");
			}
		}

		getServer().getServicesManager().unregisterAll(this);
		log("<yellow>SunGameCore 已卸载。</yellow>");
	}

	public static SunGameCorePlugin getInstance() {
		return instance;
	}

	public void log(String message) {
		getServer().getConsoleSender().sendMessage(miniMessage.deserialize("<dark_gray>[<gold>SunGameCore</gold>] </dark_gray>" + message));
	}

	private boolean checkSlimePaperEnvironment() {
		try {
			Class.forName(AdvancedSlimePaperAPI.class.getName());
			return true;
		} catch (Throwable ignored) {
			return false;
		}
	}
}
