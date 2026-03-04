package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.ItemStack;

public class NoTradingChallenge extends Challenge {

    public NoTradingChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_trading"; }

    @Override
    public String getDisplayName() { return "No Trading"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.EMERALD); }

    @Override
    public String getDescription() { return "Villager trading is disabled"; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!active) return;
        if (event.getInventory() instanceof MerchantInventory) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(Component.text("Trading is disabled!", NamedTextColor.RED));
        }
    }
}
