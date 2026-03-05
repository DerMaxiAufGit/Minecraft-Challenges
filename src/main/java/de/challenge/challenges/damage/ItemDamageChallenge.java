package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemDamageChallenge extends Challenge {

    public ItemDamageChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "item_damage"; }
    @Override public String getDisplayName() { return "Item Damage"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.CACTUS); }
    @Override public String getDescription() { return "Picking up items damages you"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player player)) return;
        double damagePerItem = plugin.getSettingsManager().getDouble("item-damage.damage-per-item", 0.5);
        int count = event.getItem().getItemStack().getAmount();
        player.damage(damagePerItem * count);
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofDouble("item-damage.damage-per-item", "Damage per Item", 0.5, 0.5, 5.0, 0.5)
        );
    }
}
