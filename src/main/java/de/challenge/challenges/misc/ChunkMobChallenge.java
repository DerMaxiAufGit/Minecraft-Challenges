package de.challenge.challenges.misc;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkMobChallenge extends Challenge {

    private static final long VALIDITY_CHECK_INTERVAL = 60L;
    private static final long PARTICLE_RENDER_INTERVAL = 5L;
    private static final int PARTICLE_Y_BELOW = 2;
    private static final int PARTICLE_Y_ABOVE = 3;
    private static final int PARTICLE_STEP = 2;
    private static final int PARTICLE_RENDER_DISTANCE_SQ = 1024;
    private static final int CHUNK_SIZE = 16;

    private static final List<EntityType> MOBS;
    private static final double BABY_CHANCE = 0.3;

    static {
        List<EntityType> mobTypes = new ArrayList<>();
        for (EntityType type : EntityType.values()) {
            if (type.isAlive() && type.isSpawnable()
                    && type != EntityType.PLAYER
                    && type != EntityType.ENDER_DRAGON) {
                mobTypes.add(type);
            }
        }
        MOBS = List.copyOf(mobTypes);
    }

    private final Map<ChunkKey, ChunkState> activeChunks = new HashMap<>();
    private final Map<UUID, ChunkKey> mobToChunk = new HashMap<>();
    private final Set<ChunkKey> clearedChunks = new HashSet<>();
    private final Map<UUID, ChunkKey> playerChunkMap = new HashMap<>();
    private final Set<UUID> graceActive = new HashSet<>();
    private final List<BukkitTask> graceTasks = new ArrayList<>();
    private BukkitTask validityCheckTask;
    private boolean mobGlow;
    private long gracePeriodTicks;

    public ChunkMobChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "chunk_mob"; }

    @Override
    public String getDisplayName() { return "Chunk = Random Mob"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.SPAWNER); }

    @Override
    public String getDescription() { return "Each new chunk spawns a mob you must defeat to leave"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.MOBS; }

    @Override
    protected void onEnable() {
        mobGlow = plugin.getSettingsManager().getBoolean("chunk-mob.mob-glow", true);
        gracePeriodTicks = plugin.getSettingsManager().getLong("chunk-mob.grace-period-ticks", 60L);

        validityCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupInvalidChunks,
                VALIDITY_CHECK_INTERVAL, VALIDITY_CHECK_INTERVAL);
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofBoolean("chunk-mob.mob-glow", "Mob Glow", true),
                ConfigurableSetting.ofLong("chunk-mob.grace-period-ticks", "Grace Period (ticks)", 60L, 0L, 200L, 10L)
        );
    }

    @Override
    protected void onDisable() {
        if (validityCheckTask != null) {
            validityCheckTask.cancel();
            validityCheckTask = null;
        }
        for (Map.Entry<ChunkKey, ChunkState> entry : activeChunks.entrySet()) {
            ChunkState state = entry.getValue();
            for (UUID playerId : state.lockedPlayers) {
                releasePlayer(playerId);
            }
            state.cleanup(true);
        }
        activeChunks.clear();
        mobToChunk.clear();
        clearedChunks.clear();
        playerChunkMap.clear();
        graceActive.clear();
        graceTasks.forEach(BukkitTask::cancel);
        graceTasks.clear();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!active) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Location from = event.getFrom();
        Location to = event.getTo();

        int fromChunkX = from.getBlockX() >> 4;
        int fromChunkZ = from.getBlockZ() >> 4;
        int toChunkX = to.getBlockX() >> 4;
        int toChunkZ = to.getBlockZ() >> 4;

        if (fromChunkX == toChunkX && fromChunkZ == toChunkZ) return;

        ChunkKey lockedIn = playerChunkMap.get(playerId);
        if (lockedIn != null) {
            if (toChunkX != lockedIn.chunkX || toChunkZ != lockedIn.chunkZ) {
                event.setCancelled(true);
                player.sendActionBar(Component.text("Kill the mob to leave this chunk!", NamedTextColor.RED));
            }
            return;
        }

        if (graceActive.contains(playerId)) return;

        ChunkKey targetChunk = new ChunkKey(player.getWorld().getUID(), toChunkX, toChunkZ);

        if (clearedChunks.contains(targetChunk)) return;

        ChunkState existing = activeChunks.get(targetChunk);
        if (existing != null) {
            lockPlayerIntoChunk(player, targetChunk, existing);
            teleportOtherPlayersToChunk(player, targetChunk, existing);
        } else {
            spawnChunkMob(player, targetChunk);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!active) return;
        Entity entity = event.getEntity();

        ChunkKey chunkKey = mobToChunk.remove(entity.getUniqueId());
        if (chunkKey == null) return;

        ChunkState state = activeChunks.remove(chunkKey);
        if (state == null) return;

        clearedChunks.add(chunkKey);

        String mobName = formatMobName(entity.getType().name());

        for (UUID playerId : state.lockedPlayers) {
            playerChunkMap.remove(playerId);
            releasePlayer(playerId);

            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.sendMessage(Component.text("Chunk cleared! ", NamedTextColor.GREEN)
                        .append(Component.text("The " + mobName + " has been defeated.", NamedTextColor.GRAY)));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
            }
        }

        state.cleanup(false);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        graceActive.remove(playerId);
        removePlayerFromChunk(playerId);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!active) return;
        applyGracePeriod(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!active) return;
        UUID playerId = event.getPlayer().getUniqueId();
        removePlayerFromChunk(playerId);
        applyGracePeriod(playerId);
    }

    private void applyGracePeriod(UUID playerId) {
        graceActive.add(playerId);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            graceActive.remove(playerId);
            graceTasks.removeIf(t -> t.isCancelled() || t.getTaskId() == -1);
        }, gracePeriodTicks);
        graceTasks.add(task);
    }

    private void spawnChunkMob(Player player, ChunkKey chunkKey) {
        EntityType mobType = MOBS.get(ThreadLocalRandom.current().nextInt(MOBS.size()));

        World world = player.getWorld();
        int baseX = chunkKey.chunkX << 4;
        int baseZ = chunkKey.chunkZ << 4;

        int spawnX = baseX + 8;
        int spawnZ = baseZ + 8;
        int spawnY = world.getHighestBlockYAt(spawnX, spawnZ) + 1;

        Location spawnLoc = new Location(world, spawnX + 0.5, spawnY, spawnZ + 0.5);

        Entity mob = world.spawnEntity(spawnLoc, mobType);

        if (mob instanceof LivingEntity livingMob) {
            if (mobGlow) {
                livingMob.setGlowing(true);
            }
            livingMob.setRemoveWhenFarAway(false);
        }

        if (mob instanceof Ageable ageable && ThreadLocalRandom.current().nextDouble() < BABY_CHANCE) {
            ageable.setBaby();
        }

        BukkitTask particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ChunkState currentState = activeChunks.get(chunkKey);
            if (currentState == null) return;
            for (UUID lockedId : currentState.lockedPlayers) {
                Player lockedPlayer = Bukkit.getPlayer(lockedId);
                if (lockedPlayer != null && lockedPlayer.isOnline()) {
                    renderChunkBorder(lockedPlayer, chunkKey.chunkX, chunkKey.chunkZ);
                }
            }
            clampMobToChunk(mob, baseX, baseZ);
        }, 0L, PARTICLE_RENDER_INTERVAL);

        ChunkState state = new ChunkState(mob, particleTask);
        activeChunks.put(chunkKey, state);
        mobToChunk.put(mob.getUniqueId(), chunkKey);

        lockPlayerIntoChunk(player, chunkKey, state);

        String mobName = formatMobName(mobType.name());
        player.sendMessage(Component.text("A ", NamedTextColor.YELLOW)
                .append(Component.text(mobName, NamedTextColor.RED))
                .append(Component.text(" has spawned! Kill it to leave this chunk.", NamedTextColor.YELLOW)));
        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 0.5f, 1.5f);

        teleportOtherPlayersToChunk(player, chunkKey, state);
    }

    private void lockPlayerIntoChunk(Player player, ChunkKey chunkKey, ChunkState state) {
        lockPlayerIntoChunk(player, chunkKey, state, true);
    }

    private void lockPlayerIntoChunk(Player player, ChunkKey chunkKey, ChunkState state, boolean notify) {
        UUID playerId = player.getUniqueId();
        state.lockedPlayers.add(playerId);
        playerChunkMap.put(playerId, chunkKey);

        int baseX = chunkKey.chunkX << 4;
        int baseZ = chunkKey.chunkZ << 4;

        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(baseX + 8.0, baseZ + 8.0);
        border.setSize(CHUNK_SIZE);
        border.setDamageBuffer(0.5);
        border.setDamageAmount(0);
        border.setWarningDistance(0);
        player.setWorldBorder(border);

        if (notify && state.lockedPlayers.size() > 1) {
            String mobName = formatMobName(state.mob.getType().name());
            player.sendMessage(Component.text("This chunk already has a ", NamedTextColor.YELLOW)
                    .append(Component.text(mobName, NamedTextColor.RED))
                    .append(Component.text("! Help kill it to leave.", NamedTextColor.YELLOW)));
            player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 0.5f, 1.5f);
        }
    }

    private void teleportOtherPlayersToChunk(Player triggeringPlayer, ChunkKey chunkKey, ChunkState state) {
        World world = Bukkit.getWorld(chunkKey.worldId);
        if (world == null) return;

        int baseX = chunkKey.chunkX << 4;
        int baseZ = chunkKey.chunkZ << 4;

        Location[] borderSpots = {
            new Location(world, baseX + 0.5, 0, baseZ + 8.5),
            new Location(world, baseX + 15.5, 0, baseZ + 8.5),
            new Location(world, baseX + 8.5, 0, baseZ + 0.5),
            new Location(world, baseX + 8.5, 0, baseZ + 15.5),
        };

        String mobName = formatMobName(state.mob.getType().name());
        int spotIndex = 0;

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.getUniqueId().equals(triggeringPlayer.getUniqueId())) continue;
            if (!other.getWorld().getUID().equals(chunkKey.worldId)) continue;
            if (graceActive.contains(other.getUniqueId())) continue;

            ChunkKey currentChunk = playerChunkMap.get(other.getUniqueId());
            if (chunkKey.equals(currentChunk)) continue;

            removePlayerFromChunk(other.getUniqueId());

            // Lock before teleporting to prevent PlayerMoveEvent re-triggering
            lockPlayerIntoChunk(other, chunkKey, state, false);

            Location spot = borderSpots[spotIndex % borderSpots.length].clone();
            int safeY = world.getHighestBlockYAt(spot.getBlockX(), spot.getBlockZ()) + 1;
            spot.setY(safeY);
            other.teleport(spot);

            other.sendMessage(Component.text("You've been pulled into a chunk! Kill the ", NamedTextColor.YELLOW)
                    .append(Component.text(mobName, NamedTextColor.RED))
                    .append(Component.text(" to escape.", NamedTextColor.YELLOW)));
            other.playSound(other.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 0.5f, 1.5f);

            spotIndex++;
        }
    }

    private void removePlayerFromChunk(UUID playerId) {
        ChunkKey chunkKey = playerChunkMap.remove(playerId);
        if (chunkKey == null) return;

        releasePlayer(playerId);

        ChunkState state = activeChunks.get(chunkKey);
        if (state == null) return;

        state.lockedPlayers.remove(playerId);

        if (state.lockedPlayers.isEmpty()) {
            activeChunks.remove(chunkKey);
            if (state.mob != null) {
                mobToChunk.remove(state.mob.getUniqueId());
            }
            state.cleanup(true);
        }
    }

    private void releasePlayer(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.setWorldBorder(null);
        }
    }

    private void cleanupInvalidChunks() {
        Iterator<Map.Entry<ChunkKey, ChunkState>> it = activeChunks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ChunkKey, ChunkState> entry = it.next();
            ChunkKey chunkKey = entry.getKey();
            ChunkState state = entry.getValue();

            state.lockedPlayers.removeIf(playerId -> {
                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline()) {
                    playerChunkMap.remove(playerId);
                    releasePlayer(playerId);
                    return true;
                }
                return false;
            });

            if (state.mob == null || state.mob.isDead() || !state.mob.isValid()) {
                clearedChunks.add(chunkKey);
                if (state.mob != null) {
                    mobToChunk.remove(state.mob.getUniqueId());
                }
                for (UUID playerId : state.lockedPlayers) {
                    playerChunkMap.remove(playerId);
                    releasePlayer(playerId);
                }
                state.cleanup(false);
                it.remove();
            } else if (state.lockedPlayers.isEmpty()) {
                mobToChunk.remove(state.mob.getUniqueId());
                state.cleanup(true);
                it.remove();
            }
        }
    }

    private void renderChunkBorder(Player player, int chunkX, int chunkZ) {
        if (!player.isOnline()) return;

        World world = player.getWorld();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int playerY = player.getLocation().getBlockY();
        int minY = playerY - PARTICLE_Y_BELOW;
        int maxY = playerY + PARTICLE_Y_ABOVE;

        Particle particle = Particle.SOUL_FIRE_FLAME;

        for (int y = minY; y <= maxY; y++) {
            for (int x = baseX; x < baseX + CHUNK_SIZE; x += PARTICLE_STEP) {
                spawnParticle(world, player, particle, x, y, baseZ);
            }
            for (int x = baseX; x < baseX + CHUNK_SIZE; x += PARTICLE_STEP) {
                spawnParticle(world, player, particle, x, y, baseZ + CHUNK_SIZE);
            }
            for (int z = baseZ; z < baseZ + CHUNK_SIZE; z += PARTICLE_STEP) {
                spawnParticle(world, player, particle, baseX, y, z);
            }
            for (int z = baseZ; z < baseZ + CHUNK_SIZE; z += PARTICLE_STEP) {
                spawnParticle(world, player, particle, baseX + CHUNK_SIZE, y, z);
            }
        }
    }

    private void spawnParticle(World world, Player player, Particle particle, double x, double y, double z) {
        Location loc = new Location(world, x + 0.5, y + 0.5, z + 0.5);
        if (loc.distanceSquared(player.getLocation()) < PARTICLE_RENDER_DISTANCE_SQ) {
            player.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    private void clampMobToChunk(Entity mob, int baseX, int baseZ) {
        if (mob == null || mob.isDead() || !mob.isValid()) return;

        Location loc = mob.getLocation();
        double x = loc.getX();
        double z = loc.getZ();

        double clampedX = Math.max(baseX + 0.5, Math.min(baseX + CHUNK_SIZE - 0.5, x));
        double clampedZ = Math.max(baseZ + 0.5, Math.min(baseZ + CHUNK_SIZE - 0.5, z));

        if (Math.abs(clampedX - x) > 1e-6 || Math.abs(clampedZ - z) > 1e-6) {
            mob.teleport(new Location(loc.getWorld(), clampedX, loc.getY(), clampedZ, loc.getYaw(), loc.getPitch()));
        }
    }

    private static String formatMobName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(' ');
            if (!words[i].isEmpty()) {
                result.append(Character.toUpperCase(words[i].charAt(0)));
                result.append(words[i].substring(1));
            }
        }
        return result.toString();
    }

    private record ChunkKey(UUID worldId, int chunkX, int chunkZ) {}

    private static final class ChunkState {
        final Entity mob;
        final BukkitTask particleTask;
        final Set<UUID> lockedPlayers = new HashSet<>();

        ChunkState(Entity mob, BukkitTask particleTask) {
            this.mob = mob;
            this.particleTask = particleTask;
        }

        void cleanup(boolean removeMob) {
            if (particleTask != null) {
                particleTask.cancel();
            }
            if (removeMob && mob != null && mob.isValid() && !mob.isDead()) {
                mob.remove();
            }
        }
    }
}
