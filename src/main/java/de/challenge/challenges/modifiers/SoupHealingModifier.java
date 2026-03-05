package de.challenge.challenges.modifiers;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SoupHealingModifier extends Challenge {

    public SoupHealingModifier(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "soup_healing"; }
    @Override public String getDisplayName() { return "Soup Healing"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.MUSHROOM_STEW); }
    @Override public String getDescription() { return "Mushroom stew heals you"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MODIFIERS; }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!active) return;
        if (event.getItem().getType() != Material.MUSHROOM_STEW) return;
        Player player = event.getPlayer();
        double healAmount = plugin.getSettingsManager().getDouble("soup-healing.heal-amount", 7.0);
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(player.getHealth() + healAmount, maxHealth));
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofDouble("soup-healing.heal-amount", "Heal Amount", 7.0, 1.0, 20.0, 1.0)
        );
    }
}
