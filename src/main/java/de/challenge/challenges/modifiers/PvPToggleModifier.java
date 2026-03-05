package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PvPToggleModifier extends Challenge {

    public PvPToggleModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "pvp_toggle"; }
    @Override public String getDisplayName() { return "PvP Toggle"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.IRON_SWORD); }
    @Override public String getDescription() { return "Toggle PvP on or off"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!active) return;
        boolean pvpEnabled = plugin.getSettingsManager().getBoolean("pvp-toggle.pvp-enabled", false);
        if (!pvpEnabled && event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofBoolean("pvp-toggle.pvp-enabled", "PvP Enabled", false)
        );
    }
}
