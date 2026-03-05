package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DamageMultiplierModifier extends Challenge {

    public DamageMultiplierModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "damage_multiplier"; }
    @Override public String getDisplayName() { return "Damage Multiplier"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.ANVIL); }
    @Override public String getDescription() { return "Multiply all damage by a factor"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!active) return;
        double multiplier = plugin.getSettingsManager().getDouble("damage-multiplier.multiplier", 2.0);
        event.setDamage(event.getDamage() * multiplier);
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofDouble("damage-multiplier.multiplier", "Multiplier", 2.0, 0.5, 10.0, 0.5)
        );
    }
}
