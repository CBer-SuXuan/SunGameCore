# SunGameCore 使用文档

SunGameCore 是一个面向 Minecraft Paper 小游戏服务器的通用库插件。它不是某个具体小游戏，而是给其他小游戏插件复用的基础框架，主要负责临时竞技场世界、匹配队列、对局模型、通用监听器、旁观者、BossBar、战利品、清理流程、命令框架、PlaceholderAPI 和常用工具。

推荐环境：

- Java 21
- AdvancedSlimePaper 1.21.11 核心（必须使用，不能使用原始的paper核心，是paper的fork）
  - Github：https://github.com/InfernalSuite/AdvancedSlimePaper
  - Docs：https://infernalsuite.com/docs/asp/
  - **Download**：https://infernalsuite.com/download/asp/
- 可选：PlaceholderAPI

---

## 1. 安装方式

将构建好的 jar 放入服务器插件目录：

```text
plugins/SunGameCore.jar
```

如果你使用临时 `.slime` 世界，需要准备模板目录：

```text
plugins/SunGameCore/template
```

示例：

```text
plugins/SunGameCore/template/queue.slime
plugins/SunGameCore/template/arena_default.slime
```

---

## 2. 通过 JitPack 引入

SunGameCore 已发布到 JitPack，其他小游戏插件可以直接通过 JitPack 引入 API。

JitPack 页面：

```text
https://jitpack.io/#CBer-SuXuan/SunGameCore/v1.2.0
```

### 2.1 Maven 示例

先添加 JitPack 仓库：

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

再添加依赖：

```xml
<dependency>
    <groupId>com.github.cber-suxuan</groupId>
    <artifactId>SunGameCore</artifactId>
    <version>v1.2.0</version>
    <scope>provided</scope>
</dependency>
```

`scope` 使用 `provided`，因为 SunGameCore 是运行时 Bukkit 插件，服务器 `plugins` 目录中仍然需要放入 SunGameCore jar。不要把 SunGameCore shade 进小游戏插件 jar。

### 2.2 Gradle 示例

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.cber-suxuan:SunGameCore:v1.2.0'
}
```

### 2.3 Paper API 示例

Paper API 示例：

```xml
<dependency>
    <groupId>io.papermc.paper</groupId>
    <artifactId>paper-api</artifactId>
    <version>1.21.11-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

---

## 3. plugin.yml 配置

小游戏插件应依赖 SunGameCore：

```yaml
name: ExampleGame
version: 1.0.0
main: your.package.ExampleGamePlugin
api-version: '1.21.11'
depend:
  - SunGameCore
softdepend:
  - PlaceholderAPI
```

---

## 4. 获取服务

SunGameCore 会注册两个 Bukkit Service：

```java
me.suxuan.slimearena.api.ArenaManager
me.suxuan.sungame.api.MiniGameService
```

在小游戏插件中获取：

```java
RegisteredServiceProvider<ArenaManager> arenaRegistration =
        getServer().getServicesManager().getRegistration(ArenaManager.class);

RegisteredServiceProvider<MiniGameService> gameRegistration =
        getServer().getServicesManager().getRegistration(MiniGameService.class);

if (arenaRegistration == null || gameRegistration == null) {
    getLogger().severe("无法获取 SunGameCore 服务。");
    getServer().getPluginManager().disablePlugin(this);
    return;
}

ArenaManager arenaManager = arenaRegistration.getProvider();
MiniGameService miniGameService = gameRegistration.getProvider();
```

---

## 5. ArenaManager：临时竞技场世界

`ArenaManager` 用于从 `.slime` 模板创建临时世界，并在游戏结束后销毁。

SunGameCore 不会为临时世界内置任何默认 Bukkit `GameRule`。所有世界规则必须由使用库的小游戏插件显式传入。如果你确实希望完全沿用模板世界原始规则，也必须显式传入：

```java
WorldRuleProfile.empty()
```

```java
public interface ArenaManager {
    CompletableFuture<World> createArenaAsync(String templateName, String instanceName, WorldRuleProfile ruleProfile);
    CompletableFuture<Void> discardArenaAsync(World world, Location fallbackLocation);
    boolean isArenaWorld(World world);
}
```

创建世界：

```java
WorldRuleProfile gameRules = WorldRuleProfile.empty()
        .set(GameRule.DO_DAYLIGHT_CYCLE, false)
        .set(GameRule.DO_WEATHER_CYCLE, false)
        .set(GameRule.DO_MOB_SPAWNING, false)
        .set(GameRule.KEEP_INVENTORY, true);

arenaManager.createArenaAsync("arena_template", "game_" + System.currentTimeMillis(), gameRules)
        .thenAccept(world -> {
            getLogger().info("创建竞技场成功: " + world.getName());
        })
        .exceptionally(throwable -> {
            getLogger().warning("创建竞技场失败: " + throwable.getMessage());
            return null;
        });
```

销毁世界：

```java
Location fallback = Bukkit.getWorlds().getFirst().getSpawnLocation();
arenaManager.discardArenaAsync(world, fallback);
```

---

## 6. MiniGameService 总览

`MiniGameService` 是小游戏功能入口：

```java
QueueManager createQueueManager(JavaPlugin owner, QueueSettings settings, QueueCallbacks callbacks);
TeleportTracker createTeleportTracker(JavaPlugin owner);
GameTaskRegistry createTaskRegistry(JavaPlugin owner);
<G extends GameSession> SpectatorService<G> createSpectatorService(JavaPlugin owner);
<G extends GameSession> GameBossBarService<G> createBossBarService(JavaPlugin owner);
<G extends GameSession> BoundaryWatcher<G> createBoundaryWatcher(JavaPlugin owner, GameTaskRegistry taskRegistry);
<G extends GameSession> GameCleanupService<G> createCleanupService(JavaPlugin owner, GameTaskRegistry taskRegistry);
```

---

## 7. QueueManager：匹配队列

### 7.1 QueueSettings

```java
WorldRuleProfile queueRules = WorldRuleProfile.empty()
        .set(GameRule.DO_DAYLIGHT_CYCLE, false)
        .set(GameRule.DO_WEATHER_CYCLE, false)
        .set(GameRule.DO_MOB_SPAWNING, false)
        .set(GameRule.KEEP_INVENTORY, true);

QueueSettings settings = new QueueSettings(
        "example",
        "queue_template",
        new Location(null, 0.5, 80, 0.5, 0, 0),
        2,
        8,
        60,
        15,
        75,
        queueRules
);
```

字段含义：

| 字段 | 说明 |
| --- | --- |
| `idPrefix` | 队列 ID 前缀 |
| `templateWorld` | queue 世界模板名称，不带 `.slime` |
| `spawn` | queue 世界内相对出生点，world 可为 null |
| `minPlayers` | 开始倒计时最低人数 |
| `maxPlayers` | 最大人数 |
| `longCountdownSeconds` | 普通倒计时 |
| `quickCountdownSeconds` | 快速倒计时 |
| `quickCountdownPercent` | 达到最大人数百分比后进入快速倒计时 |
| `worldRules` | queue 世界规则配置，必须显式传入 |

### 7.2 创建队列管理器

```java
QueueManager queueManager = miniGameService.createQueueManager(this, settings, new QueueCallbacks() {
    @Override
    public void onPlayerJoinedQueue(Player player, QueueArena queue) {
        player.sendMessage(Component.text("已加入队列"));
    }

    @Override
    public void onCountdownTick(QueueArena queue, int secondsLeft) {
        AudienceUtil.showQueueTitle(
                queue,
                Component.text(secondsLeft),
                Component.text("游戏即将开始"),
                0,
                20,
                0
        );
    }

    @Override
    public void onQueueReady(QueueArena queue) {
        startGame(queue);
    }
});
```

### 7.3 常用方法

```java
queueManager.joinQueue(player);
queueManager.joinQueueResult(player);
queueManager.leaveQueue(player);
queueManager.queueOf(player);
queueManager.queues();
queueManager.startCountdown(queue, true);
queueManager.cleanupQueue(queue, true);
queueManager.stopAll();
```

---

## 8. 对局模型

### 8.1 GameSession

小游戏对局需要实现 `GameSession`：

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

### 8.2 BaseGameSession

推荐简单对局直接继承：

```java
public final class ExampleGame extends BaseGameSession {
    public ExampleGame(String id, World world, String mapId) {
        super(id, world, mapId);
    }
}
```

---

## 9. ManagedPlayerProvider

通用监听器需要通过 `ManagedPlayerProvider` 查询玩家属于哪个游戏或队列。

```java
public final class ExampleProvider implements ManagedPlayerProvider<ExampleGame> {
    @Override
    public JavaPlugin plugin() {
        return plugin;
    }

    @Override
    public Optional<ExampleGame> gameOf(Player player) {
        return Optional.ofNullable(playerGames.get(player.getUniqueId()));
    }

    @Override
    public Optional<ExampleGame> gameByWorld(World world) {
        return games.values().stream()
                .filter(game -> game.world().equals(world))
                .findFirst();
    }

    @Override
    public Optional<QueueArena> queueOf(Player player) {
        return queueManager.queueOf(player);
    }
}
```

---

## 10. 通用监听器

### 10.1 生命周期监听器

`CommonLifecycleListener` 只负责监听生命周期事件并调用使用方提供的策略/回调。具体行为不再由库写死，必须显式传入 `LifecyclePolicy`。

`LifecyclePolicy` 可以控制：

- 是否隐藏进服/退服消息
- 进服时是否清背包、重置玩家状态
- 是否取消饥饿变化、自然回血
- 是否禁止传送门和特殊传送
- 死亡时是否清掉落、清经验、隐藏死亡消息
- 死亡淘汰原因
- 是否自动重生、自动重生延迟
- 重生后是否设置类旁观状态

```java
LifecyclePolicy<ExampleGame> lifecyclePolicy = LifecyclePolicy.defaults();

Bukkit.getPluginManager().registerEvents(
        new CommonLifecycleListener<>(provider, lifecycleCallbacks, protectionPolicy, lifecyclePolicy),
        this
);
```

如果你的小游戏需要自定义行为，可以覆盖策略方法：

```java
LifecyclePolicy<ExampleGame> lifecyclePolicy = new LifecyclePolicy<>() {
    @Override
    public boolean cancelFoodChange(Player player) {
        return false;
    }

    @Override
    public boolean prepareSpectatorLikeOnRespawn(Player player, ExampleGame game) {
        return true;
    }
};
```

### 10.2 保护监听器

`CommonProtectionListener` 处理：

- 方块破坏/放置
- 桶操作
- 爆炸破坏
- 背包交互
- queue 玩家伤害
- 淘汰玩家伤害

```java
Bukkit.getPluginManager().registerEvents(
        new CommonProtectionListener<>(provider, new ProtectionPolicy() {}),
        this
);
```

可以覆盖策略：

```java
new ProtectionPolicy() {
    @Override
    public boolean cancelBlockBreak(Player player) {
        return provider.queueOf(player).isPresent() || provider.isEliminated(player);
    }
}
```

### 10.3 聊天隔离监听器

`CommonChatListener` 可以隔离游戏聊天和队列聊天：

```java
Bukkit.getPluginManager().registerEvents(
        new CommonChatListener<>(provider, new ChatPolicy<ExampleGame>() {}),
        this
);
```

---

## 11. GameTaskRegistry：任务管理

用于按对局 ID 管理 Bukkit task。

```java
GameTaskRegistry tasks = miniGameService.createTaskRegistry(this);

tasks.repeat(game.id(), "timer", 20L, 20L, () -> {
    // 每秒执行
});

tasks.runLater(game.id(), "ending", 100L, () -> {
    // 延迟执行
});

tasks.cancel(game.id(), "timer");
tasks.cancelAll(game.id());
tasks.cancelAll();
```

---

## 12. BoundaryWatcher：边界检测

用于检测玩家是否离开竞技场、掉入虚空或超出区域。

```java
BoundaryWatcher<ExampleGame> boundaries = miniGameService.createBoundaryWatcher(this, tasks);

boundaries.watch(
        game,
        List.of(
                BoundaryRule.outsideWorld("离开竞技场"),
                BoundaryRule.belowY(-20, "掉入虚空")
        ),
        (player, currentGame, reason) -> eliminate(player, reason),
        20L,
        5L
);
```

可用规则：

```java
BoundaryRule.belowY(minY, reason)
BoundaryRule.outsideWorld(reason)
BoundaryRule.outsideWorldIgnoringPendingTeleport(teleportTracker, reason)
BoundaryRule.outsideBox(min, max, reason)
BoundaryRule.outsideRadius(center, radius, reason)
```

---

## 13. SpectatorService：旁观者服务

用于处理淘汰后的类旁观状态、可见性和离开物品。

```java
SpectatorService<ExampleGame> spectators = miniGameService.createSpectatorService(this);

spectators.makeSpectator(
        player,
        game,
        SpectatorOptions.of(game.world().getSpawnLocation(), leaveItem)
);

spectators.showToGame(player, game);
spectators.showAll(game);
spectators.clear(player);
spectators.clearAll(game);
```

---

## 14. GameBossBarService：BossBar 服务

用于按玩家维护 BossBar，适合显示队列人数、倒计时、保护期、游戏时间等。

```java
GameBossBarService<ExampleGame> bossBars = miniGameService.createBossBarService(this);

bossBars.showQueue(
        queue,
        Component.text("匹配中 " + queue.players().size()),
        queue.players().size() / 8.0F,
        BossBar.Color.BLUE
);

bossBars.showGame(
        game,
        Component.text("剩余 " + seconds + " 秒"),
        seconds / 600.0F,
        BossBar.Color.RED
);

bossBars.clear(player);
bossBars.clearGame(game);
bossBars.clearAll();
```

---

## 15. GameCleanupService：游戏清理

用于游戏结束后的玩家清理、任务取消、世界销毁。

```java
GameCleanupService<ExampleGame> cleanup = miniGameService.createCleanupService(this, tasks);

cleanup.cleanup(
        game,
        GameCleanupOptions.discardWorld(endingDelayTicks, 100L, fallbackLocation),
        (player, currentGame) -> {
            PlayerStateUtil.prepareAdventure(player);
            queueManager.joinQueue(player);
        },
        () -> spectators.showAll(game),
        () -> spectators.clearAll(game)
);
```

---

## 16. Loot 战利品系统

### 16.1 配置示例

```yaml
loot:
  default-tier: normal
  tiers:
    normal:
      min-items-per-chest: 5
      max-items-per-chest: 8
      entries:
        iron_sword:
          material: IRON_SWORD
          min-amount: 1
          max-amount: 1
          weight: 5
          name: "<gold>强化铁剑"
          enchantments:
            sharpness: 1
```

### 16.2 读取和填充

```java
LootRegistry registry = LootRegistry.fromSection(config.getConfigurationSection("loot"));
LootTable table = registry.table("normal");
table.fill(inventory, random);
```

### 16.3 按距离选择 tier

```java
DistanceLootSelector selector = DistanceLootSelector.fromSection(section, center, registry);
String tier = selector.tierFor(chestLocation);
selector.lootRegistry().table(tier).fill(inventory, random);
```

---

## 17. 命令框架

`MiniCommandExecutor` 用于快速编写子命令。

```java
MiniCommandExecutor executor = MiniCommandExecutor.builder()
        .usage("<yellow>用法: /<label> <join|leave|status>")
        .unknownMessage("<red>未知子命令")
        .playerOnlyMessage("<red>只有玩家可以执行")
        .register(MiniCommand.builder("join")
                .playerOnly(true)
                .executor(context -> {
                    Player player = context.playerOrNull();
                    if (player != null) queueManager.joinQueue(player);
                })
                .build())
        .build();
```

---

## 18. PlaceholderAPI 支持

继承 `BaseMiniGameExpansion`：

```java
public final class ExampleExpansion extends BaseMiniGameExpansion<ExampleGame> {
    public ExampleExpansion(PlaceholderValueProvider<ExampleGame> provider) {
        super(provider);
    }

    @Override
    public String getIdentifier() {
        return "example";
    }

    @Override
    public String getAuthor() {
        return "author";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
```

---

## 19. 常用工具

### 19.1 PlayerStateUtil

```java
PlayerStateUtil.reset(player);
PlayerStateUtil.healToMax(player);
PlayerStateUtil.prepareAdventure(player);
PlayerStateUtil.prepareSurvival(player);
PlayerStateUtil.prepareSpectatorLike(player);
```

### 19.2 LocationUtil

```java
Location actual = LocationUtil.withWorld(relativeLocation, world);
```

### 19.3 AudienceUtil

```java
AudienceUtil.broadcastQueue(queue, component);
AudienceUtil.broadcastGame(game, component);
AudienceUtil.showQueueTitle(queue, title, subtitle, 0, 20, 0);
AudienceUtil.showGameTitle(game, title, subtitle, 0, 20, 0);
```

### 19.4 TeleportTracker

```java
TeleportTracker tracker = miniGameService.createTeleportTracker(this);
tracker.teleport(player, target);
tracker.isPending(player, targetWorld);
tracker.clear(player);
tracker.clearAll();
```

### 19.5 GameItemUtil

用于给特殊物品打标记。

```java
GameItemUtil.setActionKey(item, "example:special_item");
GameItemUtil.isActionItem(item, "example:special_item");
GameItemUtil.actionKey(item);
```

配置物品也支持：

```yaml
special_item:
  material: BLAZE_ROD
  name: "<gold>特殊道具"
  action-key: example:special_item
```

---

## 20. 配置工具

### 20.1 ItemStackConfigUtil

支持：

- material
- amount
- name
- lore
- custom-model-data
- unbreakable
- flags
- enchantments
- attributes
- action-key

### 20.2 LocationConfigUtil

```yaml
spawn:
  x: 0.5
  y: 80
  z: 0.5
  yaw: 0
  pitch: 0
```

```java
Location location = LocationConfigUtil.readRelative(section);
```

---

## 21. 推荐小游戏结构

```text
ExampleGamePlugin
├─ onEnable()
│  ├─ 获取 ArenaManager 和 MiniGameService
│  ├─ 创建 QueueManager
│  ├─ 创建 TaskRegistry / BoundaryWatcher / CleanupService
│  ├─ 创建 SpectatorService / BossBarService
│  ├─ 注册通用监听器
│  ├─ 注册自定义玩法监听器
│  └─ 注册命令和 PlaceholderAPI
├─ GameManager
│  ├─ startGameFromQueue(queue)
│  ├─ eliminate(player, reason)
│  ├─ checkWin(game)
│  └─ endGame(game)
└─ GameSession 实现类
```

---

## 22. 构建

```bash
mvn clean package
```

构建产物：

```text
target/SunGameCore-1.0.0-SNAPSHOT.jar
```
