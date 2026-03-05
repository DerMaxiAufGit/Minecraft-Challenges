package de.challenge.challenges.projects;

import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllSoundsProject extends Project {

    private List<String> objectives;

    public AllSoundsProject(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "all_sounds"; }
    @Override public String getDisplayName() { return "All Sounds"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.NOTE_BLOCK); }
    @Override public String getDescription() { return "Trigger every sound in the game"; }

    @Override
    public List<String> getObjectives() {
        if (objectives == null) {
            objectives = new ArrayList<>();
            for (Sound sound : Sound.values()) {
                objectives.add(sound.name());
            }
            Collections.shuffle(objectives);
        }
        return objectives;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!active || complete) return;
        checkSoundTrigger(event.getPlayer());
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!active || complete) return;
        checkSoundTrigger(event.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!active || complete) return;
        if (event.getEntity() instanceof Player player) {
            checkSoundTrigger(player);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!active || complete) return;
        checkSoundTrigger(event.getPlayer());
    }

    private void checkSoundTrigger(Player player) {
        // Advance on any action since sounds are triggered by many game events
        String current = getCurrentObjective();
        if (current != null) {
            advanceObjective(player);
        }
    }
}
