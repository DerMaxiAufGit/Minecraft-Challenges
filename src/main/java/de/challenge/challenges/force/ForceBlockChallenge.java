package de.challenge.challenges.force;

import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ForceBlockChallenge extends AbstractForceChallenge {

    private static final List<Material> BLOCKS = List.of(
            Material.STONE, Material.DIRT, Material.GRASS_BLOCK, Material.SAND,
            Material.GRAVEL, Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG,
            Material.OAK_PLANKS, Material.COBBLESTONE, Material.IRON_ORE, Material.COAL_ORE,
            Material.DIAMOND_ORE, Material.OAK_LEAVES, Material.CLAY, Material.NETHERRACK,
            Material.SOUL_SAND, Material.OBSIDIAN, Material.ICE, Material.SNOW_BLOCK,
            Material.SANDSTONE, Material.RED_SAND, Material.DEEPSLATE, Material.TUFF,
            Material.GRANITE, Material.DIORITE, Material.ANDESITE, Material.MOSSY_COBBLESTONE
    );

    public ForceBlockChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "force_block"; }

    @Override
    public String getDisplayName() { return "Force Block"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.DIAMOND_ORE); }

    @Override
    public String getDescription() { return "Stand on a specific block within time limit"; }

    @Override
    protected String getConfigPrefix() { return "force-block"; }

    @Override
    protected String pickObjective() {
        return BLOCKS.get(ThreadLocalRandom.current().nextInt(BLOCKS.size())).name();
    }

    @Override
    protected boolean checkObjective(Player player) {
        Material below = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        return below.name().equals(currentObjective);
    }

    @Override
    protected String getObjectiveDisplayName() {
        return "Stand on: " + formatMaterialName(currentObjective);
    }

    @Override protected String getTaskMessageKey() { return "force-block.task"; }
    @Override protected String getSuccessMessageKey() { return "force-block.success"; }
    @Override protected String getFailMessageKey() { return "force-block.fail"; }

    private String formatMaterialName(String name) {
        return name.replace("_", " ").toLowerCase();
    }
}
