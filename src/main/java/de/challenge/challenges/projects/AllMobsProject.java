package de.challenge.challenges.projects;

import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllMobsProject extends Project {

    private List<String> objectives;

    public AllMobsProject(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "all_mobs"; }

    @Override
    public String getDisplayName() { return "All Mobs"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.DIAMOND_SWORD); }

    @Override
    public String getDescription() { return "Kill every mob type in the game"; }

    @Override
    public List<String> getObjectives() {
        if (objectives == null) {
            objectives = new ArrayList<>();
            for (EntityType type : EntityType.values()) {
                if (type.isAlive() && type != EntityType.PLAYER) {
                    objectives.add(type.name());
                }
            }
            Collections.shuffle(objectives);
        }
        return objectives;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!active || complete) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        String current = getCurrentObjective();
        if (current != null && current.equals(event.getEntity().getType().name())) {
            advanceObjective(killer);
        }
    }
}
