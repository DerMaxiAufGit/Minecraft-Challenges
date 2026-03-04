package de.challenge.challenges.misc;

import de.challenge.Challenge;
import de.challenge.ChallengePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SnakeChallenge extends Challenge {

    private final Map<UUID, LinkedList<Location>> trails = new HashMap<>();
    private Material trailBlock;
    private int maxLength;

    public SnakeChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "snake"; }

    @Override
    public String getDisplayName() { return "Snake"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.LIME_CONCRETE); }

    @Override
    public String getDescription() { return "A trail follows you - don't touch it!"; }

    @Override
    protected void onEnable() {
        String blockName = plugin.getConfig().getString("snake.block", "LIME_CONCRETE");
        try {
            trailBlock = Material.valueOf(blockName);
        } catch (IllegalArgumentException e) {
            trailBlock = Material.LIME_CONCRETE;
        }
        maxLength = plugin.getConfig().getInt("snake.max-trail-length", 20);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        // Only trigger on full block change
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        LinkedList<Location> trail = trails.computeIfAbsent(uuid, k -> new LinkedList<>());

        // Check if stepping on trail
        Block below = event.getTo().getBlock().getRelative(BlockFace.DOWN);
        if (below.getType() == trailBlock && !trail.isEmpty()) {
            // Don't punish for the block we just placed
            Location belowLoc = below.getLocation();
            if (!belowLoc.equals(trail.getLast())) {
                player.damage(player.getHealth() + 1);
                return;
            }
        }

        // Place trail at previous position
        Block prevBelow = event.getFrom().getBlock().getRelative(BlockFace.DOWN);
        if (!prevBelow.getType().isAir() && prevBelow.getType() != trailBlock
                && prevBelow.getType() != Material.BEDROCK) {
            Location loc = prevBelow.getLocation();
            prevBelow.setType(trailBlock);
            trail.addLast(loc);

            // Remove oldest if trail too long
            while (trail.size() > maxLength) {
                Location oldest = trail.removeFirst();
                Block block = oldest.getBlock();
                if (block.getType() == trailBlock) {
                    block.setType(Material.AIR);
                }
            }
        }
    }

    @Override
    protected void onDisable() {
        // Clean up all trails
        for (LinkedList<Location> trail : trails.values()) {
            for (Location loc : trail) {
                Block block = loc.getBlock();
                if (block.getType() == trailBlock) {
                    block.setType(Material.AIR);
                }
            }
        }
        trails.clear();
    }
}
