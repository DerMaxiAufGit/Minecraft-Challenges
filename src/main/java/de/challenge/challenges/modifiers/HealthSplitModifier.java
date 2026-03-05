package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class HealthSplitModifier extends Challenge {

    public HealthSplitModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "health_split"; }
    @Override public String getDisplayName() { return "Health Split"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.GOLDEN_APPLE); }
    @Override public String getDescription() { return "20 HP divided among all online players"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @Override
    protected void onEnable() {
        recalculate();
    }

    @Override
    protected void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) attr.setBaseValue(20.0);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!active) return;
        recalculate();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!active) return;
        Bukkit.getScheduler().runTaskLater(plugin, this::recalculate, 1L);
    }

    private void recalculate() {
        int count = Bukkit.getOnlinePlayers().size();
        if (count == 0) return;
        double healthPerPlayer = Math.max(2.0, 20.0 / count);
        for (Player player : Bukkit.getOnlinePlayers()) {
            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr == null) continue;
            attr.setBaseValue(healthPerPlayer);
            if (player.getHealth() > healthPerPlayer) {
                player.setHealth(healthPerPlayer);
            }
        }
    }
}
