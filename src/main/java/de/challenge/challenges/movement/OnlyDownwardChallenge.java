package de.challenge.challenges.movement;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OnlyDownwardChallenge extends Challenge {

    private final Map<UUID, Double> minY = new HashMap<>();

    public OnlyDownwardChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "only_downward"; }
    @Override public String getDisplayName() { return "Only Downward"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.ANVIL); }
    @Override public String getDescription() { return "You can never go above your lowest Y level"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.MOVEMENT; }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        if (player.isInsideVehicle() || player.isInWater() || player.isGliding()) return;

        UUID uuid = player.getUniqueId();
        double currentY = event.getTo().getY();
        double lowest = minY.getOrDefault(uuid, currentY);

        if (currentY < lowest) {
            minY.put(uuid, currentY);
        } else if (currentY > lowest + 0.1) {
            player.setHealth(0);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!active) return;
        minY.remove(event.getPlayer().getUniqueId());
    }

    @Override
    protected void onDisable() {
        minY.clear();
    }
}
