package de.challenge.challenges.projects;

import de.challenge.ChallengePlugin;
import de.challenge.DeathOverride;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllDeathMessagesProject extends Project implements DeathOverride {

    private List<String> objectives;

    public AllDeathMessagesProject(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "all_death_messages"; }

    @Override
    public String getDisplayName() { return "All Death Messages"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.SKELETON_SKULL); }

    @Override
    public String getDescription() { return "Die from every damage cause"; }

    @Override
    public boolean allowDeath() {
        return true; // This project overrides death-ends-challenge
    }

    @Override
    public List<String> getObjectives() {
        if (objectives == null) {
            objectives = new ArrayList<>();
            for (DamageCause cause : DamageCause.values()) {
                if (cause != DamageCause.CUSTOM) {
                    objectives.add(cause.name());
                }
            }
            Collections.shuffle(objectives);
        }
        return objectives;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!active || complete) return;
        Player player = event.getEntity();
        DamageCause cause = null;
        if (player.getLastDamageCause() != null) {
            cause = player.getLastDamageCause().getCause();
        }
        if (cause == null) return;

        String current = getCurrentObjective();
        if (current != null && current.equals(cause.name())) {
            advanceObjective(player);
        } else {
            player.sendMessage(Component.text(
                    "Wrong death cause! Need: " + (current != null ? current.replace("_", " ") : "none"),
                    NamedTextColor.RED));
        }
    }
}
