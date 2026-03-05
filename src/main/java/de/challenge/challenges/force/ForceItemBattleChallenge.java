package de.challenge.challenges.force;

import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ForceItemBattleChallenge extends AbstractForceChallenge {

    private static final List<Material> ITEMS;

    static {
        List<Material> items = new ArrayList<>();
        for (Material mat : Material.values()) {
            if (mat.isItem() && !mat.isAir() && !mat.name().startsWith("LEGACY_")) {
                items.add(mat);
            }
        }
        ITEMS = List.copyOf(items);
    }

    public ForceItemBattleChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "force_item_battle"; }
    @Override public String getDisplayName() { return "Force Item Battle"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.GOLDEN_SWORD); }
    @Override public String getDescription() { return "Competitive: collect the target item first"; }

    @Override protected String getConfigPrefix() { return "force-item-battle"; }

    @Override
    protected String pickObjective() {
        return ITEMS.get(ThreadLocalRandom.current().nextInt(ITEMS.size())).name();
    }

    @Override
    protected boolean checkObjective(Player player) {
        if (currentObjective == null) return false;
        Material target = Material.valueOf(currentObjective);
        return player.getInventory().contains(target);
    }

    @Override
    protected String getObjectiveDisplayName() {
        return "Collect: " + currentObjective.toLowerCase().replace("_", " ");
    }

    @Override protected String getTaskMessageKey() { return "force-item-battle.task"; }
    @Override protected String getSuccessMessageKey() { return "force-item-battle.success"; }
    @Override protected String getFailMessageKey() { return "force-item-battle.fail"; }
}
