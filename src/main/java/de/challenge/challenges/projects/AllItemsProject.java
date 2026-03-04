package de.challenge.challenges.projects;

import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllItemsProject extends Project {

    private List<String> objectives;

    public AllItemsProject(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "all_items"; }

    @Override
    public String getDisplayName() { return "All Items"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.NETHER_STAR); }

    @Override
    public String getDescription() { return "Collect every item in the game"; }

    @Override
    public List<String> getObjectives() {
        if (objectives == null) {
            objectives = new ArrayList<>();
            for (Material mat : Material.values()) {
                if (mat.isItem() && !mat.isAir() && !mat.name().startsWith("LEGACY_")) {
                    objectives.add(mat.name());
                }
            }
            Collections.shuffle(objectives);
        }
        return objectives;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!active || complete) return;
        if (!(event.getEntity() instanceof Player player)) return;
        checkItem(player, event.getItem().getItemStack().getType());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!active || complete) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        checkItem(player, event.getCurrentItem().getType());
    }

    private void checkItem(Player player, Material material) {
        String current = getCurrentObjective();
        if (current != null && current.equals(material.name())) {
            advanceObjective(player);
        }
    }
}
