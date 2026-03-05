package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class NoDuplicateItemsChallenge extends Challenge {

    public NoDuplicateItemsChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "no_duplicate_items"; }
    @Override public String getDisplayName() { return "No Duplicate Items"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.HOPPER); }
    @Override public String getDescription() { return "You cannot have two of the same item type"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player player)) return;
        Material mat = event.getItem().getItemStack().getType();
        if (hasMaterial(player, mat)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("You already have " + formatName(mat) + "!", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!active) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType().isAir()) return;

        // Block placing items into player inventory from external inventories if already owned
        if (event.getClickedInventory() instanceof PlayerInventory
                && !(event.getView().getTopInventory() instanceof PlayerInventory)
                && hasMaterial(player, cursor.getType())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("You already have " + formatName(cursor.getType()) + "!", NamedTextColor.RED));
        }
    }

    private boolean hasMaterial(Player player, Material material) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) return true;
        }
        return false;
    }

    private String formatName(Material mat) {
        return mat.name().toLowerCase().replace("_", " ");
    }
}
