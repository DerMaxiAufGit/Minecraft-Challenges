package de.challenge.challenges.environmental;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class ChunkBlockRandomizerChallenge extends Challenge {

    private static final Material[] BLOCK_TYPES = {
            Material.STONE, Material.DIRT, Material.SAND, Material.GRAVEL,
            Material.OAK_PLANKS, Material.COBBLESTONE, Material.GLASS,
            Material.BRICKS, Material.NETHERRACK, Material.END_STONE,
            Material.TERRACOTTA, Material.PRISMARINE, Material.SPONGE,
            Material.WHITE_WOOL, Material.ICE, Material.CLAY,
            Material.DEEPSLATE, Material.TUFF, Material.CALCITE
    };

    private static final int BLOCKS_PER_TICK = 4096; // process ~4K blocks per tick

    private final Set<Long> processedChunks = new HashSet<>();
    private final List<BukkitTask> pendingTasks = new ArrayList<>();

    public ChunkBlockRandomizerChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "chunk_block_randomizer"; }
    @Override public String getDisplayName() { return "Chunk Block Randomizer"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.COMMAND_BLOCK); }
    @Override public String getDescription() { return "Entering a chunk replaces all blocks with one random type"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @Override
    protected void onEnable() {
        loadState();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        if (event.getTo() == null) return;
        int fromCX = event.getFrom().getBlockX() >> 4;
        int fromCZ = event.getFrom().getBlockZ() >> 4;
        int toCX = event.getTo().getBlockX() >> 4;
        int toCZ = event.getTo().getBlockZ() >> 4;

        if (fromCX == toCX && fromCZ == toCZ) return;

        long key = ((long) toCX << 32) | (toCZ & 0xFFFFFFFFL);
        if (!processedChunks.add(key)) return;

        Player player = event.getPlayer();
        Chunk chunk = player.getWorld().getChunkAt(toCX, toCZ);
        Material replacement = BLOCK_TYPES[ThreadLocalRandom.current().nextInt(BLOCK_TYPES.length)];

        int minY = player.getWorld().getMinHeight();
        int maxY = player.getWorld().getMaxHeight();

        // Spread block replacement across multiple ticks to avoid freezing
        replaceChunkBlocks(chunk, replacement, minY, maxY);
    }

    private void replaceChunkBlocks(Chunk chunk, Material replacement, int minY, int maxY) {
        int totalHeight = maxY - minY;
        int totalBlocks = 16 * 16 * totalHeight;
        int ticksNeeded = (totalBlocks + BLOCKS_PER_TICK - 1) / BLOCKS_PER_TICK;

        for (int tick = 0; tick < ticksNeeded; tick++) {
            int startIndex = tick * BLOCKS_PER_TICK;
            int endIndex = Math.min(startIndex + BLOCKS_PER_TICK, totalBlocks);
            int tickDelay = tick;

            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!active) return;
                for (int i = startIndex; i < endIndex; i++) {
                    int y = minY + (i / 256); // 256 = 16*16
                    int remainder = i % 256;
                    int x = remainder / 16;
                    int z = remainder % 16;
                    Block block = chunk.getBlock(x, y, z);
                    Material type = block.getType();
                    if (!type.isAir() && type != Material.BEDROCK) {
                        block.setType(replacement);
                    }
                }
            }, tickDelay);
            pendingTasks.add(task);
        }
    }

    @Override
    protected void onDisable() {
        if (preservingState) {
            saveState();
        } else {
            File file = new File(plugin.getDataFolder(), "chunk_randomizer_state.yml");
            if (file.exists()) file.delete();
        }
        for (BukkitTask task : pendingTasks) {
            task.cancel();
        }
        pendingTasks.clear();
        processedChunks.clear();
    }

    private void saveState() {
        File file = new File(plugin.getDataFolder(), "chunk_randomizer_state.yml");
        FileConfiguration config = new YamlConfiguration();
        config.set("processedChunks", new ArrayList<>(processedChunks));
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save chunk randomizer state: " + e.getMessage());
        }
    }

    private void loadState() {
        File file = new File(plugin.getDataFolder(), "chunk_randomizer_state.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<Long> saved = config.getLongList("processedChunks");
        processedChunks.addAll(saved);
    }
}
