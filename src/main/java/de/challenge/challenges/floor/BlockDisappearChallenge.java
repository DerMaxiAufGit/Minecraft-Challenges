package de.challenge.challenges.floor;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlockDisappearChallenge extends Challenge {

    private final Map<Long, Set<Material>> triggeredPerChunk = new HashMap<>();

    public BlockDisappearChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "block_disappear"; }

    @Override
    public String getDisplayName() { return "Block Disappear"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.GLASS); }

    @Override
    public String getDescription() { return "Blocks you step on disappear from the entire chunk"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.FLOOR; }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        // Only trigger on full block change
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Block below = event.getTo().getBlock().getRelative(BlockFace.DOWN);
        Material type = below.getType();
        if (type.isAir() || type == Material.BEDROCK) return;

        Chunk chunk = below.getChunk();
        long chunkKey = (long) chunk.getX() << 32 | (chunk.getZ() & 0xFFFFFFFFL);

        Set<Material> triggered = triggeredPerChunk.computeIfAbsent(chunkKey, k -> new HashSet<>());
        if (triggered.contains(type)) return;
        triggered.add(type);

        // Remove all blocks of this type in the chunk
        int minY = chunk.getWorld().getMinHeight();
        int maxY = chunk.getWorld().getMaxHeight();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    Block b = chunk.getBlock(x, y, z);
                    if (b.getType() == type) {
                        b.setType(Material.AIR);
                    }
                }
            }
        }
    }

    @Override
    protected void onDisable() {
        triggeredPerChunk.clear();
    }
}
