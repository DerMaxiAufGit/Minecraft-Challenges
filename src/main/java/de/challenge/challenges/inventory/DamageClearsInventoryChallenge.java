package de.challenge.challenges.inventory;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class DamageClearsInventoryChallenge extends Challenge {

    public DamageClearsInventoryChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "damage_clears_inventory"; }

    @Override
    public String getDisplayName() { return "Damage Clears Inventory"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.LAVA_BUCKET); }

    @Override
    public String getDescription() { return "Taking damage clears your entire inventory"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.INVENTORY; }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getDamage() <= 0) return;

        // Schedule for next tick so the damage is applied first
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isDead()) {
                player.getInventory().clear();
                player.sendMessage(Component.text("Your inventory was cleared!", NamedTextColor.RED));
            }
        });
    }
}
