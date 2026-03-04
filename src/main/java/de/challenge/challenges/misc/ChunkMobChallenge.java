package de.challenge.challenges.misc;

import de.challenge.Challenge;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkMobChallenge extends Challenge {

    private static final String MOB_TAG = "chunk_challenge_mob";

    private static final long VALIDITY_CHECK_INTERVAL = 60L;
    private static final long PARTICLE_RENDER_INTERVAL = 5L;
    private static final int PARTICLE_Y_BELOW = 2;
    private static final int PARTICLE_Y_ABOVE = 3;
    private static final int PARTICLE_STEP = 2;
    private static final int PARTICLE_RENDER_DISTANCE_SQ = 1024;
    private static final int CHUNK_SIZE = 16;

    private static final List<EntityType> MOBS = List.of(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
            EntityType.CREEPER, EntityType.ENDERMAN, EntityType.WITCH,
            EntityType.BLAZE, EntityType.SLIME, EntityType.PHANTOM,
            EntityType.DROWNED, EntityType.HUSK, EntityType.STRAY,
            EntityType.WITHER_SKELETON, EntityType.PIGLIN, EntityType.HOGLIN,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.MAGMA_CUBE,
            EntityType.SILVERFISH, EntityType.CAVE_SPIDER, EntityType.VINDICATOR,
            EntityType.PILLAGER, EntityType.GUARDIAN
    );

    private final Map<UUID, ChunkLockState> lockedPlayers = new HashMap<>();
    private final Set<UUID> graceActive = new HashSet<>();
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
    protected void onEnable() {
        mobGlow = plugin.getConfig().getBoolean("chunk-mob.mob-glow", true);
        gracePeriodTicks = plugin.getConfig().getLong("chunk-mob.grace-period-ticks", 60L);

        validityCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<UUID, ChunkLockState>> it = lockedPlayers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, ChunkLockState> entry = it.next();
                UUID playerId = entry.getKey();
                ChunkLockState state = entry.getValue();

                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline()) {
                    unlockPlayer(playerId, state, true);
                    it.remove();
                    continue;
                }

                if (state.mob() == null || state.mob().isDead() || !state.mob().isValid()) {
                    unlockPlayer(playerId, state, false);
                    it.remove();
                }
            }
        }, VALIDITY_CHECK_INTERVAL, VALIDITY_CHECK_INTERVAL);
    }

    @Override
    protected void onDisable() {
        if (validityCheckTask != null) {
            validityCheckTask.cancel();
            validityCheckTask = null;
        }
        for (Map.Entry<UUID, ChunkLockState> entry : lockedPlayers.entrySet()) {
            unlockPlayer(entry.getKey(), entry.getValue(), true);
        }
        lockedPlayers.clear();
        graceActive.clear();
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

        ChunkLockState state = lockedPlayers.get(playerId);

        if (state != null) {
            if (toChunkX != state.chunkX() || toChunkZ != state.chunkZ()) {
                event.setCancelled(true);
                player.sendActionBar(Component.text("Kill the mob to leave this chunk!", NamedTextColor.RED));
            }
            return;
        }

        if (graceActive.contains(playerId)) return;

        spawnChunkMob(player, toChunkX, toChunkZ);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!active) return;
        Entity entity = event.getEntity();
        if (!entity.hasMetadata(MOB_TAG)) return;

        List<MetadataValue> metadata = entity.getMetadata(MOB_TAG);
        if (metadata.isEmpty()) return;

        UUID ownerId;
        try {
            ownerId = UUID.fromString(metadata.get(0).asString());
        } catch (IllegalArgumentException e) {
            return;
        }

        ChunkLockState state = lockedPlayers.remove(ownerId);
        if (state == null) return;

        unlockPlayer(ownerId, state, false);

        Player player = Bukkit.getPlayer(ownerId);
        if (player != null && player.isOnline()) {
            String mobName = formatMobName(entity.getType().name());
            player.sendMessage(Component.text("Mob defeated! ", NamedTextColor.GREEN)
                    .append(Component.text("You killed the " + mobName + ".", NamedTextColor.GRAY)));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        graceActive.remove(playerId);
        ChunkLockState state = lockedPlayers.remove(playerId);
        if (state != null) {
            unlockPlayer(playerId, state, true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!active) return;
        applyGracePeriod(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!active) return;
        applyGracePeriod(event.getPlayer().getUniqueId());
    }

    private void applyGracePeriod(UUID playerId) {
        graceActive.add(playerId);
        Bukkit.getScheduler().runTaskLater(plugin, () -> graceActive.remove(playerId), gracePeriodTicks);
    }

    private void spawnChunkMob(Player player, int chunkX, int chunkZ) {
        EntityType mobType = MOBS.get(ThreadLocalRandom.current().nextInt(MOBS.size()));

        World world = player.getWorld();
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        int spawnX = Math.max(baseX + 1, Math.min(baseX + CHUNK_SIZE - 2, player.getLocation().getBlockX() + 2));
        int spawnZ = Math.max(baseZ + 1, Math.min(baseZ + CHUNK_SIZE - 2, player.getLocation().getBlockZ() + 2));
        int spawnY = world.getHighestBlockYAt(spawnX, spawnZ) + 1;

        Location spawnLoc = new Location(world, spawnX + 0.5, spawnY, spawnZ + 0.5);

        Entity mob = world.spawnEntity(spawnLoc, mobType);
        mob.setMetadata(MOB_TAG, new FixedMetadataValue(plugin, player.getUniqueId().toString()));

        if (mob instanceof LivingEntity livingMob) {
            if (mobGlow) {
                livingMob.setGlowing(true);
            }
            livingMob.setRemoveWhenFarAway(false);
        }

        BukkitTask particleTask = Bukkit.getScheduler().runTaskTimer(plugin,
                () -> renderChunkBorder(player, chunkX, chunkZ), 0L, PARTICLE_RENDER_INTERVAL);

        ChunkLockState state = new ChunkLockState(chunkX, chunkZ, mob, particleTask);
        lockedPlayers.put(player.getUniqueId(), state);

        String mobName = formatMobName(mobType.name());
        player.sendMessage(Component.text("A ", NamedTextColor.YELLOW)
                .append(Component.text(mobName, NamedTextColor.RED))
                .append(Component.text(" has spawned! Kill it to leave this chunk.", NamedTextColor.YELLOW)));
        player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 0.5f, 1.5f);
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
            for (int x = baseX; x <= baseX + CHUNK_SIZE; x += PARTICLE_STEP) {
                spawnParticle(world, player, particle, x, y, baseZ);
            }
            for (int x = baseX; x <= baseX + CHUNK_SIZE; x += PARTICLE_STEP) {
                spawnParticle(world, player, particle, x, y, baseZ + CHUNK_SIZE);
            }
            for (int z = baseZ; z <= baseZ + CHUNK_SIZE; z += PARTICLE_STEP) {
                spawnParticle(world, player, particle, baseX, y, z);
            }
            for (int z = baseZ; z <= baseZ + CHUNK_SIZE; z += PARTICLE_STEP) {
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

    private void unlockPlayer(UUID playerId, ChunkLockState state, boolean removeMob) {
        if (state.particleTask() != null) {
            state.particleTask().cancel();
        }
        if (removeMob && state.mob() != null && state.mob().isValid() && !state.mob().isDead()) {
            state.mob().remove();
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

    private record ChunkLockState(int chunkX, int chunkZ, Entity mob, BukkitTask particleTask) {}
}
