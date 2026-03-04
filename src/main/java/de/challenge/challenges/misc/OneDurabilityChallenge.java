package de.challenge.challenges.misc;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class OneDurabilityChallenge extends Challenge {

    public OneDurabilityChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "one_durability"; }

    @Override
    public String getDisplayName() { return "One Durability"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.WOODEN_PICKAXE); }

    @Override
    public String getDescription() { return "Items break after one more use"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.INVENTORY; }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (!active) return;
        ItemStack item = event.getItem();
        if (item.getItemMeta() instanceof Damageable damageable) {
            int maxDurability = item.getType().getMaxDurability();
            if (maxDurability > 0) {
                damageable.setDamage(maxDurability - 1);
                item.setItemMeta(damageable);
                event.setDamage(0); // We already set it
            }
        }
    }
}
