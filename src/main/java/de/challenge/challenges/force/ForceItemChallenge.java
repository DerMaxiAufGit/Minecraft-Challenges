package de.challenge.challenges.force;

import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ForceItemChallenge extends AbstractForceChallenge {

    private static final List<Material> ITEMS = List.of(
            Material.COBBLESTONE, Material.OAK_LOG, Material.IRON_INGOT,
            Material.DIAMOND, Material.COAL, Material.STICK, Material.BREAD,
            Material.APPLE, Material.ARROW, Material.BONE, Material.STRING,
            Material.LEATHER, Material.FEATHER, Material.FLINT, Material.WHEAT,
            Material.SUGAR_CANE, Material.EGG, Material.PORKCHOP, Material.BEEF,
            Material.CHICKEN, Material.COD, Material.SALMON, Material.GOLD_INGOT,
            Material.REDSTONE, Material.LAPIS_LAZULI, Material.EMERALD,
            Material.GLOWSTONE_DUST, Material.CLAY_BALL, Material.SNOWBALL
    );

    public ForceItemChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "force_item"; }

    @Override
    public String getDisplayName() { return "Force Item"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.CHEST); }

    @Override
    public String getDescription() { return "Get a specific item within time limit"; }

    @Override
    protected String getConfigPrefix() { return "force-item"; }

    @Override
    protected String pickObjective() {
        return ITEMS.get(ThreadLocalRandom.current().nextInt(ITEMS.size())).name();
    }

    @Override
    protected boolean checkObjective(Player player) {
        Material target = Material.valueOf(currentObjective);
        return player.getInventory().contains(target);
    }

    @Override
    protected String getObjectiveDisplayName() {
        return "Get item: " + currentObjective.replace("_", " ").toLowerCase();
    }

    @Override protected String getTaskMessageKey() { return "force-item.task"; }
    @Override protected String getSuccessMessageKey() { return "force-item.success"; }
    @Override protected String getFailMessageKey() { return "force-item.fail"; }
}
