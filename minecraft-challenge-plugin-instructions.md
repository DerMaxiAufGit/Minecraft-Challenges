# Minecraft Challenge Plugin — Agent Instructions

> **Goal:** Build a PaperMC/Spigot plugin that replicates the BastiGHG-style challenge format.
> The player must **kill the Ender Dragon** to win. If the player **dies**, the challenge is lost (configurable).
> One or more challenges (modifiers) can be active simultaneously.

---

## Project Setup

### Build System

Use **Maven** with the following `pom.xml` base:

```xml
<dependencies>
  <dependency>
    <groupId>io.papermc.paper</groupId>
    <artifactId>paper-api</artifactId>
    <version>1.21.1-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
  </dependency>
</dependencies>

<repositories>
  <repository>
    <id>papermc</id>
    <url>https://repo.papermc.io/repository/maven-public/</url>
  </repository>
</repositories>
```

### plugin.yml

```yaml
name: ChallengePlugin
version: 1.0.0
main: de.challenge.ChallengePlugin
api-version: "1.21"
commands:
  challenge:
    description: Open the challenge GUI
    aliases: [c, challenges]
  timer:
    description: Manage the challenge timer
  settings:
    description: Alias for /challenge
    aliases: [settings]
  reset:
    description: Reset the challenge world
```

### Package Structure

```
de.challenge/
├── ChallengePlugin.java          # Main plugin class
├── ChallengeManager.java         # Manages active challenges & game state
├── TimerManager.java             # Countdown/countup timer logic
├── gui/
│   └── ChallengeGUI.java         # Chest GUI for toggling challenges
├── commands/
│   ├── ChallengeCommand.java
│   ├── TimerCommand.java
│   └── ResetCommand.java
├── challenges/
│   ├── Challenge.java            # Abstract base class
│   ├── randomizer/
│   │   ├── BlockRandomizerChallenge.java
│   │   ├── CraftingRandomizerChallenge.java
│   │   └── MobDropRandomizerChallenge.java
│   ├── force/
│   │   ├── ForceBlockChallenge.java
│   │   ├── ForceMobChallenge.java
│   │   ├── ForceItemChallenge.java
│   │   ├── ForceBiomeChallenge.java
│   │   └── ForceHeightChallenge.java
│   ├── floor/
│   │   ├── FloorIsLavaChallenge.java
│   │   ├── IceFloorChallenge.java
│   │   └── BlockDisappearChallenge.java
│   ├── movement/
│   │   ├── TrafficLightChallenge.java
│   │   ├── NoSneakChallenge.java
│   │   ├── NoJumpChallenge.java
│   │   └── AlwaysRunningChallenge.java
│   ├── damage/
│   │   ├── ReversedDamageChallenge.java
│   │   ├── AchievementDamageChallenge.java
│   │   └── RandomPotionOnDamageChallenge.java
│   ├── restrictions/
│   │   ├── NoCraftingChallenge.java
│   │   ├── NoXPChallenge.java
│   │   ├── NoPlaceChallenge.java
│   │   ├── NoBreakChallenge.java
│   │   └── NoTradingChallenge.java
│   ├── misc/
│   │   ├── SnakeChallenge.java
│   │   ├── AnvilRainChallenge.java
│   │   ├── RandomMLGChallenge.java
│   │   ├── OneDurabilityChallenge.java
│   │   ├── DoubleSpawnChallenge.java
│   │   └── CoronaChallenge.java
│   └── projects/
│       ├── AllItemsProject.java
│       ├── AllMobsProject.java
│       ├── AllAdvancementsProject.java
│       └── AllDeathMessagesProject.java
└── util/
    ├── RandomizerMap.java        # Persistent block/mob/crafting mappings
    └── MessageUtil.java
```

---

## Core Architecture

### `Challenge.java` — Abstract Base

Every challenge extends this class:

```java
public abstract class Challenge implements Listener {

    protected final ChallengePlugin plugin;
    protected boolean active = false;

    public Challenge(ChallengePlugin plugin) {
        this.plugin = plugin;
    }

    public abstract String getId();          // e.g. "block_randomizer"
    public abstract String getDisplayName(); // e.g. "Block Randomizer"
    public abstract ItemStack getIcon();     // GUI icon
    public abstract String getDescription(); // Short description for GUI

    public void enable() {
        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        onEnable();
    }

    public void disable() {
        active = false;
        HandlerList.unregisterAll(this);
        onDisable();
    }

    protected void onEnable() {}  // Override to run setup logic
    protected void onDisable() {} // Override to run cleanup logic

    public boolean isActive() { return active; }
}
```

### `ChallengeManager.java`

- Holds a `Map<String, Challenge> registeredChallenges`
- Holds a `Set<String> activeChallenges`
- Provides `enable(String id)`, `disable(String id)`, `isActive(String id)`
- Listens for `EntityDeathEvent` on the Ender Dragon → calls `onChallengeWon()`
- Listens for `PlayerDeathEvent` → calls `onChallengeLost()` (if death-ends-challenge is enabled)
- On win/loss: broadcast title + sound, stop timer, optionally reset world

### `TimerManager.java`

- Tracks elapsed time using a repeating `BukkitTask` (every 20 ticks = 1 second)
- Supports `start()`, `pause()`, `resume()`, `reset()`
- Displays time as a Boss Bar (use `BossBar` API) at the top of the screen
- Timer counts **up** by default (configurable to count down)
- Time format: `HH:MM:SS`
- Color: GREEN → YELLOW (after 1h) → RED (after 2h)

---

## Challenge Implementations

### 1. Block Randomizer

**File:** `BlockRandomizerChallenge.java`

- On `enable()`: generate a `HashMap<Material, Material>` mapping every mineable block to a random drop. Store this map persistently in a YAML file (`randomizer_blocks.yml`) so it survives restarts.
- Listen to `BlockBreakEvent`: cancel the default drops, spawn the mapped item instead.
- Exclude: Bedrock, End Portal Frame, Command Blocks, air.
- On `disable()`: delete the mapping file so a new seed is used next time.

```java
@EventHandler
public void onBlockBreak(BlockBreakEvent event) {
    if (!active) return;
    event.setDropItems(false);
    Material mapped = blockMap.getOrDefault(event.getBlock().getType(), Material.AIR);
    if (mapped != Material.AIR) {
        event.getBlock().getWorld().dropItemNaturally(
            event.getBlock().getLocation(), new ItemStack(mapped));
    }
}
```

---

### 2. Crafting Randomizer

**File:** `CraftingRandomizerChallenge.java`

- On `enable()`: generate a `HashMap<Material, Material>` mapping every craftable recipe result to a random Material. Store in `randomizer_crafting.yml`.
- Listen to `PrepareItemCraftEvent`: replace `event.getInventory().setResult()` with the mapped item.
- Also randomize furnace smelting via `FurnaceBurnEvent` or by registering fake recipes.
- Note: Mapping must be consistent within a session (same input → same output always).

---

### 3. Mob Drop Randomizer

**File:** `MobDropRandomizerChallenge.java`

- On `enable()`: generate a `HashMap<EntityType, Material>` mapping every killable mob to a random item. Store in `randomizer_mobs.yml`.
- Listen to `EntityDeathEvent`: clear `event.getDrops()`, add the mapped item.
- Exclude players from the mapping.

---

### 4. Force Block

**File:** `ForceBlockChallenge.java`

- Every N seconds (configurable, default 3 minutes), pick a random block type from a predefined list and send it to the player via a Title message and action bar.
- The player has M seconds (configurable, default 30 seconds) to stand on that block.
- Track via a repeating task checking `player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType()`.
- If time runs out without standing on the block: apply configured punishment (damage, kill, or end challenge).
- Show a countdown in the action bar.

---

### 5. Force Mob

**File:** `ForceMobChallenge.java`

- Every N minutes, pick a random `EntityType` (only hostile/neutral mobs) and display it as a task.
- Player must kill that mob type within the time limit.
- Track via `EntityDeathEvent` filtered by mob type and killer == player.
- Show remaining time in action bar.

---

### 6. Force Item

**File:** `ForceItemChallenge.java`

- Every N minutes, pick a random `Material` and display it as a task.
- Player must have that item in their inventory within the time limit.
- Track via `PlayerPickupItemEvent` or poll inventory every 5 seconds.

---

### 7. Force Biome

**File:** `ForceBiomeChallenge.java`

- Every N minutes, pick a random `Biome` and display it.
- Player must physically be in that biome within the time limit.
- Poll player location every 5 seconds to check `player.getLocation().getBlock().getBiome()`.

---

### 8. Force Height

**File:** `ForceHeightChallenge.java`

- Every N minutes, display a target Y level (e.g., "stand above Y=100").
- Player must be at or above (or at exact ± tolerance) that Y level within the time limit.

---

### 9. Floor is Lava

**File:** `FloorIsLavaChallenge.java`

- Listen to `PlayerMoveEvent`.
- When a player steps on a block, schedule a task:
  - After X ticks: replace block with MAGMA_BLOCK
  - After X more ticks: replace with LAVA
  - Optionally: after another delay, restore original block (configurable)
- Keep a set of already-converted blocks to avoid double-processing.
- Exclude: Nether, The End (can be configured).

```java
@EventHandler
public void onMove(PlayerMoveEvent event) {
    if (!active) return;
    Block below = event.getTo().getBlock().getRelative(BlockFace.DOWN);
    if (convertedBlocks.contains(below.getLocation())) return;
    convertedBlocks.add(below.getLocation());
    Material original = below.getType();
    Bukkit.getScheduler().runTaskLater(plugin, () -> below.setType(Material.MAGMA_BLOCK), lavaDelayTicks);
    Bukkit.getScheduler().runTaskLater(plugin, () -> below.setType(Material.LAVA), lavaDelayTicks * 2);
    if (restoreBlocks) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            below.setType(original);
            convertedBlocks.remove(below.getLocation());
        }, lavaDelayTicks * 4);
    }
}
```

---

### 10. Ice Floor

**File:** `IceFloorChallenge.java`

- Listen to `PlayerMoveEvent`.
- Place PACKED_ICE or BLUE_ICE under the player's feet continuously.
- Toggle on/off via player sneaking (`PlayerToggleSneakEvent`).
- Optionally melt ice after a delay using a scheduled task.

---

### 11. Block Disappear in Chunk

**File:** `BlockDisappearChallenge.java`

- Listen to `PlayerMoveEvent`.
- When the player steps on a block type they haven't stepped on in the current chunk: remove ALL blocks of that type in the current chunk (16x16, all Y levels).
- Use `chunk.getChunkSnapshot()` to find all matching blocks, then iterate and set to AIR.
- Track which block types have been triggered per chunk in a `Map<ChunkCoord, Set<Material>>`.

---

### 12. Traffic Light (Ampel)

**File:** `TrafficLightChallenge.java`

- Display a Boss Bar that cycles: GREEN (can move) → YELLOW (warning) → RED (must stop).
- Timings configurable (default: 20s green, 5s yellow, 10s red).
- During RED: listen to `PlayerMoveEvent` — if the player moves (X/Z changes), kill them or apply punishment.
- Show current color via BossBar color (`BarColor.GREEN`, `BarColor.YELLOW`, `BarColor.RED`).

---

### 13. No Sneak

**File:** `NoSneakChallenge.java`

- Listen to `PlayerToggleSneakEvent`.
- If `event.isSneaking() == true`: cancel event or apply punishment (configurable).

---

### 14. No Jump

**File:** `NoJumpChallenge.java`

- Listen to `PlayerMoveEvent`.
- Compare `event.getFrom().getY()` vs `event.getTo().getY()` while player is on the ground.
- If Y increases and player was on ground → punish.
- Alternative: use `PlayerJumpEvent` (Paper API).

---

### 15. Always Running

**File:** `AlwaysRunningChallenge.java`

- Every second, check if the player has moved since the last tick (compare location).
- If the player has not moved for more than N seconds: apply punishment.
- Exclude: when player is in a GUI, sleeping, dead.

---

### 16. Reversed Damage

**File:** `ReversedDamageChallenge.java`

- Listen to `EntityDamageByEntityEvent`.
- If the damager is the player: with configurable probability (default 50%), also apply the same damage to the player.

```java
@EventHandler
public void onDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) return;
    if (Math.random() < reflectChance) {
        player.damage(event.getDamage());
    }
}
```

---

### 17. Achievement Damage

**File:** `AchievementDamageChallenge.java`

- Listen to `PlayerAdvancementDoneEvent`.
- On every new advancement: deal 1.0 damage (half a heart) to the player.
- Optionally: show which advancement triggered the damage in chat.

---

### 18. Random Potion on Damage

**File:** `RandomPotionOnDamageChallenge.java`

- Listen to `EntityDamageEvent` where entity is a Player.
- On taking damage: apply a random `PotionEffectType` for a random duration (5–30 seconds) and random amplifier (0–2).
- Pick from a curated list (avoid instant effects like HEAL/HARM that cause infinite loops).

---

### 19. No Crafting

**File:** `NoCraftingChallenge.java`

- Listen to `CraftItemEvent`.
- Cancel the event.
- Send the player a message: "Crafting is disabled in this challenge!"
- Exception: allow crafting End Eyes if needed to enter the stronghold (configurable toggle).

---

### 20. No XP

**File:** `NoXPChallenge.java`

- Listen to `PlayerExpChangeEvent`.
- If `event.getAmount() > 0`: set to 0 (cancel gaining XP).
- Or: apply punishment and end challenge.

---

### 21. No Block Place / No Block Break

**Files:** `NoPlaceChallenge.java`, `NoBreakChallenge.java`

- `BlockPlaceEvent` / `BlockBreakEvent` → cancel and punish.
- Exceptions (No Place): allow placing End Eyes, water/lava buckets, flint-and-steel (needed to complete the game).
- Exceptions (No Break): none by default, but configurable.

---

### 22. No Trading

**File:** `NoTradingChallenge.java`

- Listen to `TradeSelectEvent` or `InventoryClickEvent` where the inventory is a MerchantInventory.
- Cancel the event.

---

### 23. Snake

**File:** `SnakeChallenge.java`

- On every player movement: place a LIME_CONCRETE (or configurable material) block at the player's previous position.
- Store the trail in a `LinkedList<Location>` (max length configurable, default 20 blocks).
- If the trail exceeds max length: remove the oldest block (set to AIR).
- Listen to `PlayerMoveEvent`: if the player walks onto a trail block → punishment.
- Clear the trail on player death / challenge end.

---

### 24. Anvil Rain

**File:** `AnvilRainChallenge.java`

- Every N seconds (configurable, default 10): spawn a falling ANVIL block above the player at Y + 20 (or configurable height).
- Use `World.spawnFallingBlock()`.
- Optionally: spawn multiple anvils in a radius around the player.

---

### 25. Random MLG

**File:** `RandomMLGChallenge.java`

- In random intervals (configurable, e.g. between 10 and 15 minutes): teleport the player upward by N blocks (configurable, default 40).
- The player must land safely (e.g., place water, use ender pearl, use slime block).
- Detect survival via `PlayerMoveEvent` tracking Y decrease + landing.
- Give the player a warning 10 seconds before via title.

---

### 26. One Durability

**File:** `OneDurabilityChallenge.java`

- Listen to `PlayerItemDamageEvent`.
- Override: set item durability to max - 1 after every use (so one more hit breaks it).
- Alternative: on `PlayerInteractEvent` / attack events, manually set the item's durability to `maxDurability - 1`.

---

### 27. Double Spawn

**File:** `DoubleSpawnChallenge.java`

- Listen to `CreatureSpawnEvent`.
- When a mob spawns naturally: spawn an identical second mob at the same location.
- Use a flag to prevent infinite recursion (set a metadata tag on spawned duplicates).

```java
@EventHandler
public void onSpawn(CreatureSpawnEvent event) {
    if (event.getEntity().hasMetadata("duplicate")) return;
    if (event.getSpawnReason() == SpawnReason.NATURAL || ...) {
        LivingEntity clone = (LivingEntity) event.getLocation().getWorld()
            .spawnEntity(event.getLocation(), event.getEntityType());
        clone.setMetadata("duplicate", new FixedMetadataValue(plugin, true));
    }
}
```

---

### 28. Corona Challenge

**File:** `CoronaChallenge.java`

- Every 2 seconds: check distance from player to all nearby mobs.
- If any mob is within 2 blocks: apply a "sick" effect (POISON or WITHER, short duration, low amplifier).
- Show a warning in the action bar: "⚠ Keep your distance!"

---

## Long-Term Projects

Projects are separate from challenges — they don't require killing the Ender Dragon. Instead, they track a list of objectives and show progress.

### Base: `Project.java`

```java
public abstract class Project {
    public abstract String getId();
    public abstract String getDisplayName();
    public abstract List<String> getObjectives();     // ordered list
    public abstract int getCurrentIndex();            // which objective is next
    public abstract void onObjectiveComplete(Player player, int index);
    public abstract boolean isComplete();
}
```

Progress is stored in a YAML file per project, keyed by objective index.

---

### Project: All Items

- Show a list of every obtainable `Material` in a random or sequential order.
- Listen to `PlayerPickupItemEvent` and `InventoryClickEvent` to detect item collection.
- When the player picks up the current required item, advance to the next.
- Display current objective in the Boss Bar or action bar.

---

### Project: All Mobs

- Sequential list of every `EntityType` (only killable mobs).
- Listen to `EntityDeathEvent` where killer == player.
- Advance when the current mob type is killed.

---

### Project: All Advancements

- Use `Bukkit.advancementIterator()` to get all advancements.
- Listen to `PlayerAdvancementDoneEvent`.
- Track completed advancements per player in a set.
- Show count: "Advancements: 42 / 103"
- In team mode: if one player gets it, mark it for all.

---

### Project: All Death Messages

- Maintain an ordered list of all Minecraft death message types (fall, lava, drown, arrow, etc.).
- Use `PlayerDeathEvent` and parse `event.getDeathMessage()` or map cause via `EntityDamageEvent.DamageCause`.
- The player must die from each cause in sequence.
- Allow respawning — the challenge does not end on death for this project.

---

## GUI System

### `ChallengeGUI.java`

- Open a chest inventory (3 or 6 rows) on `/challenge`.
- Each challenge occupies one slot with:
  - Its icon `ItemStack`
  - Display name (GREEN if active, RED if inactive)
  - Lore: description + current status
- Clicking a slot toggles the challenge on/off via `ChallengeManager`.
- Use `InventoryClickEvent` to handle clicks — always cancel the event to prevent item theft.
- Add a "Start" / "Stop" button (e.g., a green/red dye in the last slot).
- Add navigation for multiple pages if there are more than 45 challenges.

---

## Timer System

### Display

- Use a persistent `BossBar` shown to all players.
- Format: `⏱ 00:14:32 | Challenges: Block Randomizer, Floor is Lava`
- Color changes based on elapsed time.

### Commands

```
/timer start         — Start the timer (also starts all active challenges)
/timer pause         — Pause timer and freeze all challenge tasks
/timer resume        — Resume
/timer reset         — Reset timer to 00:00:00 and disable all challenges
/timer set <HH:MM:SS> — Set a specific time
```

---

## Configuration

### `config.yml`

```yaml
# Game settings
death-ends-challenge: true        # Does player death lose the challenge?
respawn-on-loss: true             # Spectator mode or kicked on loss?
allow-multiple-challenges: true   # Can multiple challenges run at once?

# Timer
timer-direction: up               # "up" (countup) or "down" (countdown)
timer-start-value: "00:00:00"     # Used for countdown

# Challenge defaults
force-block:
  interval-seconds: 180
  time-limit-seconds: 30
  punishment: damage              # "damage", "kill", "end"

force-mob:
  interval-seconds: 300
  time-limit-seconds: 60
  punishment: damage

traffic-light:
  green-seconds: 20
  yellow-seconds: 5
  red-seconds: 10

floor-is-lava:
  magma-delay-ticks: 30
  lava-delay-ticks: 60
  restore-blocks: true

random-mlg:
  min-interval-seconds: 600
  max-interval-seconds: 900
  height: 40
  warning-seconds: 10

reversed-damage:
  reflect-chance: 0.5

corona:
  distance-blocks: 2
  check-interval-ticks: 40

snake:
  max-trail-length: 20
  block: LIME_CONCRETE
```

---

## Data Persistence

- Save active challenges, timer state, and randomizer mappings to `plugins/ChallengePlugin/` on `onDisable()`.
- Load them back on `onEnable()`.
- Use Bukkit's `FileConfiguration` (YAML) for all persistent data.
- Randomizer maps are stored as `Material_name: Material_name` pairs in their respective YAML files.

---

## World Reset

The `/reset` command should:
1. Kick all players.
2. Delete the current world folder (or use Multiverse-Core if available).
3. Recreate the world.
4. Teleport players back to spawn on rejoin.
5. Reset the timer and all challenge states.

For safety: require confirmation (`/reset confirm`) before executing.

---

## Messages & Localization

All messages should be stored in `messages.yml` and support both German (`de`) and English (`en`). Use a `MessageUtil` class to look up keys with player-locale awareness.

Example keys:
```yaml
en:
  challenge.won: "§a§lChallenge Complete! Time: {time}"
  challenge.lost: "§c§lChallenge Failed! You died."
  force-block.task: "§eStand on: §f{block} §e(§f{time}s§e remaining)"
  timer.paused: "§7Timer paused."

de:
  challenge.won: "§a§lChallenge geschafft! Zeit: {time}"
  challenge.lost: "§c§lChallenge verloren! Du bist gestorben."
  force-block.task: "§eSteh auf: §f{block} §e(§f{time}s §eübrig)"
  timer.paused: "§7Timer pausiert."
```

---

## Implementation Notes

- **Always cancel `BlockBreakEvent` drops** before manually spawning items — avoid `setDropItems(false)` being overridden by other listeners.
- **Randomizer maps must be seeded once** and remain stable. Never regenerate mid-session unless explicitly reset.
- **Force challenges must be skippable** (configurable) in case a target is unobtainable (e.g., biome not in seed).
- **Track tasks with `BukkitTask` references** so they can be cancelled cleanly on `disable()`.
- **Use `Bukkit.getScheduler().runTask()`** (sync) for all block modifications.
- **Test with Paper 1.21.x** — prefer Paper-specific APIs (e.g., `PlayerJumpEvent`) over workarounds.
- **Tag duplicate-spawned entities** with metadata to prevent event feedback loops.
- **GUI inventory names** must be unique per player to prevent cross-player GUI interactions.
