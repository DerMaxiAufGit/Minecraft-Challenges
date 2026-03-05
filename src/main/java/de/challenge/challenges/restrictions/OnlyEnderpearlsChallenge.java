package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class OnlyEnderpearlsChallenge extends Challenge {

    public OnlyEnderpearlsChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "only_enderpearls"; }
    @Override public String getDisplayName() { return "Only Enderpearls"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.ENDER_PEARL); }
    @Override public String getDescription() { return "You can only move by throwing enderpearls"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        Player player = event.getPlayer();
        if (!player.isOnGround()) return;
        if (player.isInsideVehicle()) return;
        if (player.isInWater()) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        if (dx * dx + dz * dz > 0.0001) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onXP(PlayerExpChangeEvent event) {
        if (!active) return;
        int xpPerPearl = plugin.getSettingsManager().getInt("only-enderpearls.xp-per-pearl", 5);
        int xp = event.getAmount();
        int pearls = xp / xpPerPearl;
        if (pearls > 0) {
            event.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL, pearls));
        }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("only-enderpearls.xp-per-pearl", "XP per Pearl", 5, 1, 50, 1)
        );
    }
}
