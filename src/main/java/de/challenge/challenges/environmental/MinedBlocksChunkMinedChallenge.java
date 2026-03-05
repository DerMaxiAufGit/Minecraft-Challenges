package de.challenge.challenges.environmental;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class MinedBlocksChunkMinedChallenge extends Challenge {

    private static final int BLOCKS_PER_TICK = 4096;
    private final List<BukkitTask> pendingTasks = new ArrayList<>();

    public MinedBlocksChunkMinedChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "mined_blocks_chunk"; }
    @Override public String getDisplayName() { return "Chunk-Wide Mining"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.IRON_PICKAXE); }
    @Override public String getDescription() { return "Mining a block removes all of that type in the chunk"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!active) return;
        Block broken = event.getBlock();
        Material type = broken.getType();
        Chunk chunk = broken.getChunk();
        int brokenX = broken.getX();
        int brokenY = broken.getY();
        int brokenZ = broken.getZ();

        int minY = broken.getWorld().getMinHeight();
        int maxY = broken.getWorld().getMaxHeight();
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
                    int y = minY + (i / 256);
                    int remainder = i % 256;
                    int x = remainder / 16;
                    int z = remainder % 16;
                    Block block = chunk.getBlock(x, y, z);
                    if (block.getType() == type
                            && !(block.getX() == brokenX && block.getY() == brokenY && block.getZ() == brokenZ)) {
                        block.setType(Material.AIR);
                    }
                }
            }, tickDelay);
            pendingTasks.add(task);
        }
    }

    @Override
    protected void onDisable() {
        for (BukkitTask task : pendingTasks) {
            task.cancel();
        }
        pendingTasks.clear();
    }
}
