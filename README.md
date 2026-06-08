# SunGameCore 使用文档

SunGameCore 是一个面向 Minecraft Paper 小游戏服务器的通用核心库插件。它提供临时竞技场世界、匹配队列、对局模型、生命周期监听器、保护监听器、独立聊天、PlaceholderAPI 支持和常用工具类，帮助开发者更快编写小游戏插件。

> 推荐用于基于 Paper `1.21.11`、Java `21`、AdvancedSlimePaper 的小游戏服务器。

---

## 1. SunGameCore 是什么

SunGameCore 不是某一个具体小游戏，而是一个给其他小游戏插件调用的 lib 插件。

它主要解决小游戏开发中的通用问题：

- 从模板 `.slime` 世界快速创建临时竞技场世界
- 游戏结束后安全卸载并丢弃临时世界
- 自动清理临时世界中的残留玩家
- 为不同小游戏创建独立队列管理器
- 玩家自动加入队列
- 队列人数达到条件后自动倒计时
- 支持快速开始和满员快速倒计时
- 玩家加入队列后自动清背包、重置状态、回血并传送
- 提供通用对局接口和基础对局类
- 提供通用生命周期监听器
- 提供通用保护监听器
- 提供游戏内/queue 内独立聊天监听器
- 提供 PlaceholderAPI 占位符基类
- 提供常用工具类，减少重复代码

---

## 2. 功能概览

### 2.1 临时竞技场世界

SunGameCore 基于 AdvancedSlimePaper 管理临时小游戏世界：

- 读取模板 `.slime` 世界
- 异步复制模板世界
- 创建临时世界实例
- 自动设置常用 GameRule
- 游戏结束后丢弃世界，不保存地图改动
- 可选清理 WorldGuard 的世界缓存
- 插件卸载时清理残留临时世界

模板目录：

```text
plugins/SunGameCore/template
```

例如：

```text
plugins/SunGameCore/template/spleef_map.slime
plugins/SunGameCore/template/tntrun_map.slime
```

### 2.2 小游戏队列

SunGameCore 可以为每个小游戏创建独立的 `QueueManager`。

队列系统支持：

- 自动创建 queue 世界
- 自动选择可加入队列
- 队列不存在时自动创建
- 最小人数倒计时
- 快速人数倒计时
- 满员 3 秒倒计时
- 玩家离开队列
- 空队列自动清理
- 队列倒计时结束后回调给小游戏插件创建正式对局

### 2.3 通用监听器

SunGameCore 提供多个可选监听器：

| 监听器 | 作用 |
| --- | --- |
| `CommonLifecycleListener` | 处理进服、退服、死亡、重生、饥饿、回血、基础交互限制 |
| `CommonProtectionListener` | 处理破坏、放置、桶、背包、爆炸、伤害保护 |
| `CommonChatListener` | 隔离游戏聊天和 queue 聊天 |

### 2.4 PlaceholderAPI 支持

如果服务器安装 PlaceholderAPI，可以继承 `BaseMiniGameExpansion` 快速注册小游戏占位符。

---

## 3. 安装要求

### 3.1 必需环境

- 核心：AdvancedSlimePaper（兼容任何Paper插件）
- Java：`21`

注：本插件必须使用 AdvancedSlimePaper 核心，下载链接在下方，插件启动时会检查 AdvancedSlimePaper API。如果环境不存在，插件会自动禁用。

相关链接：

- Github：https://github.com/InfernalSuite/AdvancedSlimePaper
- Docs：https://infernalsuite.com/docs/asp/
- **Download**：https://infernalsuite.com/download/asp/

### 3.2 可选插件

- `PlaceholderAPI`：用于占位符支持。

### 3.3 不建议插件

- `WorldGuard`：由于WorldGuard会针对该插件每一个世界进行保护并生成一个文件在插件文件夹下，如果世界被卸载掉之后，目前没有办法能将该文件夹直接删除，所以不建议使用WorldGuard，后续或许会加入兼容。

---

## 4. 服务端安装

将构建好的 jar 放入服务端：

```text
plugins/SunGameCore-1.0.0-SNAPSHOT.jar
```

首次启动后，建议准备模板目录：

```text
plugins/SunGameCore/template
```

然后把小游戏地图模板 `.slime` 文件放入该目录。

---

## 5. 在小游戏插件中引入 SunGameCore

### 5.1 Maven 依赖

```xml
<dependency>
    <groupId>me.suxuan</groupId>
    <artifactId>SunGameCore</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Paper API：

```xml
<dependency>
    <groupId>io.papermc.paper</groupId>
    <artifactId>paper-api</artifactId>
    <version>1.21.11-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### 5.2 plugin.yml

```yaml
name: ExampleMiniGame
version: 1.0.0
main: your.package.ExampleMiniGamePlugin
api-version: '1.21.11'
depend:
  - SunGameCore
```

如果你的小游戏插件使用 PlaceholderAPI：

```yaml
softdepend:
  - PlaceholderAPI
```

---

## 6. SunGameCore 提供的服务

SunGameCore 启动后会注册两个 Bukkit Service：

```java
me.suxuan.slimearena.api.ArenaManager
me.suxuan.sungame.api.MiniGameService
```

| 服务 | 作用 |
| --- | --- |
| `ArenaManager` | 创建和销毁临时竞技场世界 |
| `MiniGameService` | 创建队列管理器、创建传送追踪器 |

---

## 7. 使用 ArenaManager

`ArenaManager` 用于直接管理临时竞技场世界。

### 7.1 获取 ArenaManager

```java
import me.suxuan.slimearena.api.ArenaManager;
import org.bukkit.plugin.RegisteredServiceProvider;

RegisteredServiceProvider<ArenaManager> registration =
        getServer().getServicesManager().getRegistration(ArenaManager.class);

if (registration == null) {
    getLogger().severe("无法获取 ArenaManager，请确认 SunGameCore 已启用。");
    getServer().getPluginManager().disablePlugin(this);
    return;
}

ArenaManager arenaManager = registration.getProvider();
```

### 7.2 ArenaManager API

```java
public interface ArenaManager {
    CompletableFuture<World> createArenaAsync(String templateName, String instanceName);
    CompletableFuture<Void> discardArenaAsync(World world, Location fallbackLocation);
    boolean isArenaWorld(World world);
}
```

### 7.3 创建临时世界

假设模板文件为：

```text
plugins/SunGameCore/template/spleef_template.slime
```

创建临时世界：

```java
arenaManager.createArenaAsync("spleef_template", "spleef_game_001")
        .thenAccept(world -> {
            getLogger().info("临时竞技场创建成功: " + world.getName());
        })
        .exceptionally(throwable -> {
            getLogger().warning("临时竞技场创建失败: " + throwable.getMessage());
            return null;
        });
```

参数说明：

| 参数 | 说明 |
| --- | --- |
| `templateName` | 模板世界名称，不带 `.slime` 后缀 |
| `instanceName` | 临时世界实例名称，需要保证唯一 |

### 7.4 销毁临时世界

```java
Location fallback = Bukkit.getWorld("world").getSpawnLocation();

arenaManager.discardArenaAsync(world, fallback)
        .thenRun(() -> getLogger().info("临时竞技场已销毁"))
        .exceptionally(throwable -> {
            getLogger().warning("临时竞技场销毁失败: " + throwable.getMessage());
            return null;
        });
```

如果 `fallbackLocation` 为 `null`，临时世界内仍然存在的玩家会被踢出服务器。

### 7.5 判断世界是否为临时竞技场

```java
if (arenaManager.isArenaWorld(player.getWorld())) {
    player.sendMessage(Component.text("你正在临时竞技场世界中"));
}
```

---

## 8. 使用 MiniGameService

`MiniGameService` 是小游戏队列和通用工具服务。

### 8.1 获取 MiniGameService

```java
import me.suxuan.sungame.api.MiniGameService;
import org.bukkit.plugin.RegisteredServiceProvider;

RegisteredServiceProvider<MiniGameService> registration =
        getServer().getServicesManager().getRegistration(MiniGameService.class);

if (registration == null) {
    getLogger().severe("无法获取 MiniGameService，请确认 SunGameCore 已启用。");
    getServer().getPluginManager().disablePlugin(this);
    return;
}

MiniGameService miniGameService = registration.getProvider();
```

### 8.2 MiniGameService API

```java
public interface MiniGameService {
    QueueManager createQueueManager(JavaPlugin owner, QueueSettings settings, QueueCallbacks callbacks);
    TeleportTracker createTeleportTracker(JavaPlugin owner);
}
```

---

## 9. 队列系统

队列系统是 SunGameCore 的核心功能之一。它负责玩家匹配、临时 queue 世界创建、倒计时和队列清理。

### 9.1 QueueSettings

```java
public record QueueSettings(
        String idPrefix,
        String templateWorld,
        Location spawn,
        int minPlayers,
        int maxPlayers,
        int longCountdownSeconds,
        int quickCountdownSeconds,
        int quickCountdownPercent
) {}
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `idPrefix` | 队列世界 ID 前缀，例如 `spleef` |
| `templateWorld` | 模板 `.slime` 世界名称，不带后缀 |
| `spawn` | 玩家进入 queue 世界后的出生点，world 可为 null |
| `minPlayers` | 开始倒计时所需最小人数 |
| `maxPlayers` | 队列最大人数 |
| `longCountdownSeconds` | 普通倒计时秒数 |
| `quickCountdownSeconds` | 快速倒计时秒数 |
| `quickCountdownPercent` | 达到最大人数百分之多少后进入快速倒计时 |

快速开始人数计算：

```java
quickStartPlayers = max(minPlayers, ceil(maxPlayers * quickCountdownPercent / 100.0))
```

### 9.2 创建 QueueManager

```java
QueueSettings settings = new QueueSettings(
        "spleef",
        "spleef_queue_template",
        new Location(null, 0.5, 80, 0.5, 0, 0),
        2,
        8,
        60,
        15,
        75
);

QueueManager queueManager = miniGameService.createQueueManager(this, settings, new QueueCallbacks() {
    @Override
    public void onQueueCreated(QueueArena queue) {
        getLogger().info("队列创建成功: " + queue.id());
    }

    @Override
    public void onPlayerJoinedQueue(Player player, QueueArena queue) {
        player.sendMessage(Component.text("你已加入队列：" + queue.players().size() + "/" + queueManager.maxPlayers()));
    }

    @Override
    public void onPlayerLeftQueue(Player player, QueueArena queue) {
        player.sendMessage(Component.text("你已离开队列"));
    }

    @Override
    public void onCountdownTick(QueueArena queue, int secondsLeft) {
        AudienceUtil.showQueueTitle(
                queue,
                Component.text("游戏即将开始"),
                Component.text(secondsLeft + " 秒"),
                0,
                20,
                0
        );
    }

    @Override
    public void onCountdownCancelled(QueueArena queue) {
        AudienceUtil.broadcastQueue(queue, Component.text("人数不足，倒计时已取消"));
    }

    @Override
    public void onQueueReady(QueueArena queue) {
        startGameFromQueue(queue);
    }

    @Override
    public void onQueueCreateFailed(Throwable throwable) {
        getLogger().warning("队列创建失败: " + throwable.getMessage());
    }
});
```

### 9.3 QueueManager 常用方法

```java
queueManager.joinQueue(player);
queueManager.leaveQueue(player);
queueManager.queueOf(player);
queueManager.queues();
queueManager.cleanupQueue(queue, true);
queueManager.stopAll();
```

| 方法 | 说明 |
| --- | --- |
| `createQueue()` | 异步创建一个新的 queue 世界 |
| `joinQueue(player)` | 自动加入可用队列，没有则自动创建 |
| `addToQueue(player, queue)` | 加入指定队列 |
| `leaveQueue(player)` | 离开当前队列 |
| `removePlayer(player, cleanupEmptyQueue)` | 从队列移除玩家 |
| `startCountdown(queue, force)` | 开始倒计时，`force=true` 时强制 3 秒 |
| `cleanupQueue(queue, discardWorld)` | 清理队列，可选择是否销毁世界 |
| `queueOf(player)` | 查询玩家所在队列 |
| `queues()` | 获取当前队列快照 |
| `updateSettings(settings)` | 更新队列配置 |
| `stopAll()` | 停止所有队列 |

### 9.4 QueueArena

`QueueArena` 表示一个临时 queue 世界实例：

```java
public final class QueueArena {
    public String id();
    public World world();
    public Set<UUID> players();
    public QueueState state();
}
```

队列状态：

```java
WAITING
STARTING
CLOSED
```

### 9.5 队列流程

`joinQueue(player)` 大致会执行：

1. 检查玩家是否已经在队列中
2. 查找已有可加入队列
3. 如果没有可加入队列，创建新的 queue 世界
4. 将玩家加入队列
5. 清空玩家背包
6. 重置玩家状态并回血
7. 设置玩家为冒险模式
8. 传送到 queue 世界出生点
9. 人数达到 `minPlayers` 后开始普通倒计时
10. 人数达到 `quickStartPlayers` 后进入快速倒计时
11. 人数达到 `maxPlayers` 后进入 3 秒倒计时
12. 倒计时结束后触发 `onQueueReady(queue)`

---

## 10. 从队列创建正式游戏

SunGameCore 不强制规定你的正式游戏逻辑。推荐在 `onQueueReady(queue)` 中创建自己的游戏对局对象。

```java
private final Map<UUID, ExampleGameSession> playerGames = new HashMap<>();
private final Map<String, ExampleGameSession> games = new HashMap<>();

private void startGameFromQueue(QueueArena queue) {
    ExampleGameSession game = new ExampleGameSession(
            queue.id().replace("_queue_", "_game_"),
            queue.world(),
            "default_map"
    );

    game.state(GameState.RUNNING);
    game.players().addAll(queue.players());
    game.alivePlayers().addAll(queue.players());

    games.put(game.id(), game);

    for (UUID uuid : game.players()) {
        playerGames.put(uuid, game);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            PlayerStateUtil.prepareSurvival(player);
            player.sendMessage(Component.text("游戏开始！"));
        }
    }

    // 正式游戏继续使用 queue.world()，所以这里不要销毁世界
    queueManager.cleanupQueue(queue, false);
}
```

如果 queue 世界不再需要，可以调用：

```java
queueManager.cleanupQueue(queue, true);
```

---

## 11. 对局模型

### 11.1 GameSession

`GameSession` 是通用小游戏对局视图：

```java
public interface GameSession {
    String id();
    World world();
    String mapId();
    Set<UUID> players();
    Set<UUID> alivePlayers();
    GameState state();
    void state(GameState state);
}
```

### 11.2 BaseGameSession

如果你的小游戏不需要复杂对局类，可以直接继承 `BaseGameSession`：

```java
public final class ExampleGameSession extends BaseGameSession {
    public ExampleGameSession(String id, World world, String mapId) {
        super(id, world, mapId);
    }

    // 可以继续添加积分、队伍、回合数、地图配置等字段
}
```

### 11.3 GameState

```java
WAITING
STARTING
RUNNING
ENDING
```

---

## 12. ManagedPlayerProvider

`ManagedPlayerProvider` 用于告诉 SunGameCore：玩家当前属于哪个游戏或队列。

```java
public interface ManagedPlayerProvider<G extends GameSession> {
    JavaPlugin plugin();
    Optional<G> gameOf(Player player);
    Optional<G> gameByWorld(World world);
    Optional<QueueArena> queueOf(Player player);

    default boolean isManaged(Player player) {
        return gameOf(player).isPresent() || queueOf(player).isPresent();
    }

    default boolean isEliminated(Player player) {
        return gameOf(player)
                .map(game -> !game.alivePlayers().contains(player.getUniqueId()))
                .orElse(false);
    }
}
```

示例：

```java
public final class ExamplePlayerProvider implements ManagedPlayerProvider<ExampleGameSession> {
    private final ExampleMiniGamePlugin plugin;

    public ExamplePlayerProvider(ExampleMiniGamePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public JavaPlugin plugin() {
        return plugin;
    }

    @Override
    public Optional<ExampleGameSession> gameOf(Player player) {
        return Optional.ofNullable(plugin.getPlayerGames().get(player.getUniqueId()));
    }

    @Override
    public Optional<ExampleGameSession> gameByWorld(World world) {
        return plugin.getGames().values().stream()
                .filter(game -> game.world().equals(world))
                .findFirst();
    }

    @Override
    public Optional<QueueArena> queueOf(Player player) {
        return plugin.getQueueManager().queueOf(player);
    }
}
```

---

## 13. 通用生命周期监听器

`CommonLifecycleListener` 用于处理小游戏中常见的玩家生命周期事件。

注册示例：

```java
getServer().getPluginManager().registerEvents(
        new CommonLifecycleListener<>(provider, new LifecycleCallbacks<ExampleGameSession>() {
            @Override
            public boolean autoJoinQueue(Player player) {
                return true;
            }

            @Override
            public void joinQueue(Player player) {
                queueManager.joinQueue(player);
            }

            @Override
            public void handleQuit(Player player) {
                queueManager.leaveQueue(player);
                ExampleGameSession game = playerGames.remove(player.getUniqueId());
                if (game != null) eliminate(player, "退出游戏");
            }

            @Override
            public void eliminate(Player player, String reason) {
                ExampleGameSession game = playerGames.get(player.getUniqueId());
                if (game == null) return;
                game.alivePlayers().remove(player.getUniqueId());
                PlayerStateUtil.prepareSpectatorLike(player);
                checkGameEnd(game);
            }

            @Override
            public Location respawnLocation(Player player, ExampleGameSession game) {
                return game.world().getSpawnLocation();
            }
        }),
        this
);
```

它会处理：

- 玩家进服
- 自动入队
- 饥饿锁定
- 自然回血限制
- 禁止丢物品、捡物品、副手交换等
- 禁止传送门、末影珍珠、紫颂果等特殊传送
- 死亡后调用淘汰回调
- 自动重生并设置类旁观者状态
- 玩家退服回调

---

## 14. 通用保护监听器

`CommonProtectionListener` 用于保护 queue 和游戏世界。

注册示例：

```java
getServer().getPluginManager().registerEvents(
        new CommonProtectionListener<>(provider, new ProtectionPolicy() {}),
        this
);
```

默认策略较强：

- 禁止桶操作
- 禁止打开/点击/拖拽背包
- 禁止放置方块
- 禁止破坏方块
- 清空游戏世界爆炸破坏列表
- 取消 queue 玩家伤害
- 取消淘汰玩家伤害
- 取消 ENDING 阶段玩家伤害

自定义示例：

```java
new ProtectionPolicy() {
    @Override
    public boolean cancelBlockBreak(Player player) {
        // queue 和淘汰玩家不能破坏方块，游戏中存活玩家允许破坏
        return provider.queueOf(player).isPresent() || provider.isEliminated(player);
    }
}
```

---

## 15. 独立聊天监听器

`CommonChatListener` 用于隔离游戏和 queue 的聊天。

注册示例：

```java
getServer().getPluginManager().registerEvents(
        new CommonChatListener<>(provider, new ChatPolicy<ExampleGameSession>() {}),
        this
);
```

默认规则：

- 游戏内聊天只给同一个 `GameSession` 的玩家看
- queue 聊天只给同一个 `QueueArena` 的玩家看
- 其他游戏、其他 queue、大厅都看不到
- 未处于游戏或 queue 的玩家聊天不处理
- 控制台默认也看不到隔离聊天

允许控制台查看隔离聊天：

```java
new ChatPolicy<ExampleGameSession>() {
    @Override
    public boolean allowConsoleSeeGameChat(Player player, ExampleGameSession game) {
        return true;
    }

    @Override
    public boolean allowConsoleSeeQueueChat(Player player, QueueArena queue) {
        return true;
    }
}
```

只隔离游戏聊天，不隔离 queue 聊天：

```java
new ChatPolicy<ExampleGameSession>() {
    @Override
    public boolean isolateQueueChat(Player player, QueueArena queue) {
        return false;
    }
}
```

---

## 16. PlaceholderAPI 支持

如果服务器安装了 PlaceholderAPI，可以继承 `BaseMiniGameExpansion` 快速注册占位符。

### 16.1 默认占位符

假设 identifier 是 `examplegame`：

| 占位符 | 说明 |
| --- | --- |
| `%examplegame_area_type%` | 玩家所在区域：`game`、`queue`、`none` |
| `%examplegame_area_id%` | 当前游戏或队列 ID |
| `%examplegame_map%` | 地图 ID，队列中返回 `queue` |
| `%examplegame_alive%` | 当前游戏存活人数 |
| `%examplegame_players%` | 当前游戏或队列人数 |
| `%examplegame_queue_players%` | 当前队列人数 |
| `%examplegame_max_players%` | 最大人数 |
| `%examplegame_min_players%` | 最小人数 |
| `%examplegame_eliminated%` | 玩家是否已淘汰 |

### 16.2 创建 Expansion

```java
public final class ExampleExpansion extends BaseMiniGameExpansion<ExampleGameSession> {
    public ExampleExpansion(PlaceholderValueProvider<ExampleGameSession> provider) {
        super(provider);
    }

    @Override
    public String getIdentifier() {
        return "examplegame";
    }

    @Override
    public String getAuthor() {
        return "YourName";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
```

注册：

```java
if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
    new ExampleExpansion(new ExamplePlaceholderProvider(this)).register();
}
```

---

## 17. 工具类

### 17.1 PlayerStateUtil

```java
PlayerStateUtil.reset(player);
PlayerStateUtil.healToMax(player);
PlayerStateUtil.prepareAdventure(player);
PlayerStateUtil.prepareSurvival(player);
PlayerStateUtil.prepareSpectatorLike(player);
```

| 方法 | 说明 |
| --- | --- |
| `reset` | 重置飞行、无敌、火焰、冰冻、饱食度、经验、药水效果等 |
| `healToMax` | 回满血 |
| `prepareAdventure` | 清背包、重置、回血、设置冒险模式 |
| `prepareSurvival` | 清背包、重置、回血、设置生存模式 |
| `prepareSpectatorLike` | 设置类旁观者状态：可飞行、无敌、无碰撞 |

### 17.2 AudienceUtil

```java
AudienceUtil.broadcastQueue(queue, Component.text("队列消息"));
AudienceUtil.broadcastGame(game, Component.text("游戏消息"));
AudienceUtil.showQueueTitle(queue, title, subtitle, 10, 40, 10);
AudienceUtil.showGameTitle(game, title, subtitle, 10, 40, 10);
```

### 17.3 LocationUtil

```java
Location actual = LocationUtil.withWorld(relativeLocation, queue.world());
```

### 17.4 TeleportTracker

```java
TeleportTracker tracker = miniGameService.createTeleportTracker(this);
tracker.teleport(player, target);
tracker.isPending(player, targetWorld);
tracker.clear(player);
tracker.clearAll();
```

`TeleportTracker` 适合用于区分“插件内部传送”和“玩家主动离开竞技场”。

---

## 18. 推荐插件结构

```text
ExampleMiniGamePlugin
├─ onEnable()
│  ├─ 获取 MiniGameService
│  ├─ 创建 QueueManager
│  ├─ 创建 TeleportTracker
│  ├─ 创建 ManagedPlayerProvider
│  ├─ 注册 CommonLifecycleListener
│  ├─ 注册 CommonProtectionListener
│  ├─ 注册 CommonChatListener
│  ├─ 注册命令，例如 /join /leave /start
│  └─ 注册 PlaceholderAPI Expansion
├─ Map<UUID, ExampleGameSession> playerGames
├─ Map<String, ExampleGameSession> games
├─ startGameFromQueue(QueueArena queue)
├─ eliminate(Player player, String reason)
├─ checkGameEnd(ExampleGameSession game)
└─ endGame(ExampleGameSession game)
```

---

## 19. 最小初始化示例

```java
public final class ExampleMiniGamePlugin extends JavaPlugin {
    private MiniGameService miniGameService;
    private QueueManager queueManager;
    private TeleportTracker teleportTracker;

    @Override
    public void onEnable() {
        RegisteredServiceProvider<MiniGameService> registration =
                getServer().getServicesManager().getRegistration(MiniGameService.class);

        if (registration == null) {
            getLogger().severe("无法获取 SunGameCore 服务。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.miniGameService = registration.getProvider();
        this.teleportTracker = miniGameService.createTeleportTracker(this);

        setupQueue();

        ExamplePlayerProvider provider = new ExamplePlayerProvider(this);

        getServer().getPluginManager().registerEvents(
                new CommonLifecycleListener<>(provider, lifecycleCallbacks()),
                this
        );

        getServer().getPluginManager().registerEvents(
                new CommonProtectionListener<>(provider, new ProtectionPolicy() {}),
                this
        );

        getServer().getPluginManager().registerEvents(
                new CommonChatListener<>(provider, new ChatPolicy<ExampleGameSession>() {}),
                this
        );
    }
}
```

---

## 20. API 包结构

```text
me.suxuan.slimearena.api
└─ ArenaManager

me.suxuan.sungame
└─ SunGameCorePlugin

me.suxuan.sungame.api
├─ MiniGameService
├─ queue
│  ├─ QueueManager
│  ├─ QueueSettings
│  ├─ QueueCallbacks
│  ├─ QueueArena
│  └─ QueueState
├─ session
│  ├─ GameSession
│  ├─ BaseGameSession
│  ├─ GameState
│  └─ ManagedPlayerProvider
├─ listener
│  ├─ CommonLifecycleListener
│  ├─ LifecycleCallbacks
│  ├─ CommonProtectionListener
│  ├─ ProtectionPolicy
│  ├─ CommonChatListener
│  └─ ChatPolicy
└─ placeholder
   ├─ BaseMiniGameExpansion
   └─ PlaceholderValueProvider

me.suxuan.sungame.util
├─ AudienceUtil
├─ LocationUtil
├─ PlayerStateUtil
└─ TeleportTracker
```

---

## 21. 构建 SunGameCore

在项目根目录执行：

```bash
mvn clean package
```

构建产物：

```text
target/SunGameCore-1.0.0-SNAPSHOT.jar
```

将 jar 放入服务端 `plugins` 目录即可。
