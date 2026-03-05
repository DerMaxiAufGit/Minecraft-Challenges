package de.challenge.challenges.movement;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class NoWASDChallenge extends Challenge {

    public NoWASDChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "no_wasd"; }
    @Override public String getDisplayName() { return "No WASD"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.STRUCTURE_VOID); }
    @Override public String getDescription() { return "You cannot walk, only use other movement"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MOVEMENT; }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        Player player = event.getPlayer();
        if (player.isInsideVehicle()) return;
        if (player.isGliding()) return;
        if (player.isInWater() && plugin.getSettingsManager().getBoolean("no-wasd.allow-swim", true)) return;
        if (!player.isOnGround()) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        if (dx * dx + dz * dz > 0.0001) {
            event.setCancelled(true);
        }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofBoolean("no-wasd.allow-swim", "Allow Swimming", true)
        );
    }
}
