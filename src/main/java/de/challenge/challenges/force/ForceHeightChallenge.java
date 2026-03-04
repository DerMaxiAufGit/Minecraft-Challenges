package de.challenge.challenges.force;

import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class ForceHeightChallenge extends AbstractForceChallenge {

    private int targetY;

    public ForceHeightChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "force_height"; }

    @Override
    public String getDisplayName() { return "Force Height"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.LADDER); }

    @Override
    public String getDescription() { return "Reach a specific Y level within time limit"; }

    @Override
    protected String getConfigPrefix() { return "force-height"; }

    @Override
    protected String pickObjective() {
        targetY = ThreadLocalRandom.current().nextInt(-60, 256);
        return String.valueOf(targetY);
    }

    @Override
    protected boolean checkObjective(Player player) {
        int playerY = player.getLocation().getBlockY();
        return Math.abs(playerY - targetY) <= 2;
    }

    @Override
    protected String getObjectiveDisplayName() {
        return "Reach Y: " + targetY;
    }

    @Override protected String getTaskMessageKey() { return "force-height.task"; }
    @Override protected String getSuccessMessageKey() { return "force-height.success"; }
    @Override protected String getFailMessageKey() { return "force-height.fail"; }
}
