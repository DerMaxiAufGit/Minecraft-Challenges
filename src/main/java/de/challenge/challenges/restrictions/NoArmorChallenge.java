package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class NoArmorChallenge extends Challenge {

    public NoArmorChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "no_armor"; }

    @Override
    public String getDisplayName() { return "No Armor"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.IRON_CHESTPLATE); }

    @Override
    public String getDescription() { return "Cannot equip armor"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!active) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryType.SlotType slotType = event.getSlotType();
        if (slotType == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
            player.sendMessage(Component.text("You cannot equip armor!", NamedTextColor.RED));
        }
    }
}
