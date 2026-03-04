package de.challenge.challenges.floor;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class FloorIsLavaChallenge extends Challenge {

    private final Set<Location> convertedBlocks = new HashSet<>();

    public FloorIsLavaChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "floor_is_lava"; }

    @Override
    public String getDisplayName() { return "Floor is Lava"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.LAVA_BUCKET); }

    @Override
    public String getDescription() { return "Blocks you walk on turn to lava"; }

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
        if (below.getType().isAir() || below.isLiquid()) return;
        if (convertedBlocks.contains(below.getLocation())) return;

        Material original = below.getType();
        if (original == Material.BEDROCK || original == Material.END_PORTAL_FRAME
                || original == Material.END_PORTAL) return;

        convertedBlocks.add(below.getLocation());

        int magmaDelay = plugin.getConfig().getInt("floor-is-lava.magma-delay-ticks", 30);
        int lavaDelay = plugin.getConfig().getInt("floor-is-lava.lava-delay-ticks", 60);
        boolean restore = plugin.getConfig().getBoolean("floor-is-lava.restore-blocks", true);

        Bukkit.getScheduler().runTaskLater(plugin, () -> below.setType(Material.MAGMA_BLOCK), magmaDelay);
        Bukkit.getScheduler().runTaskLater(plugin, () -> below.setType(Material.LAVA), lavaDelay);

        if (restore) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                below.setType(original);
                convertedBlocks.remove(below.getLocation());
            }, lavaDelay * 2L);
        }
    }

    @Override
    protected void onDisable() {
        convertedBlocks.clear();
    }
}
