package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class NoEnchantingChallenge extends Challenge {

    public NoEnchantingChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_enchanting"; }

    @Override
    public String getDisplayName() { return "No Enchanting"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.ENCHANTING_TABLE); }

    @Override
    public String getDescription() { return "Cannot enchant items"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        if (!active) return;
        event.setCancelled(true);
        event.getEnchanter().sendMessage(Component.text("Enchanting is disabled!", NamedTextColor.RED));
    }

    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
        if (!active) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory() instanceof AnvilInventory)) return;

        // Block taking result from anvil
        if (event.getSlotType() == InventoryType.SlotType.RESULT) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Enchanting is disabled!", NamedTextColor.RED));
        }
    }
}
