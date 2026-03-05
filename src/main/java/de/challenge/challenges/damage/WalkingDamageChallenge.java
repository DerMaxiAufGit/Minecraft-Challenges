package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WalkingDamageChallenge extends Challenge {

    private final Map<UUID, Double> distanceAccumulated = new HashMap<>();

    public WalkingDamageChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "walking_damage"; }
    @Override public String getDisplayName() { return "Walking Damage"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.LEATHER_BOOTS); }
    @Override public String getDescription() { return "Take damage for every N blocks walked"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active) return;
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.01) return;

        UUID uuid = player.getUniqueId();
        double accumulated = distanceAccumulated.getOrDefault(uuid, 0.0) + dist;

        double blocksPerDamage = plugin.getSettingsManager().getDouble("walking-damage.blocks-per-damage", 50.0);
        double damageAmount = plugin.getSettingsManager().getDouble("walking-damage.damage-amount", 1.0);

        while (accumulated >= blocksPerDamage) {
            accumulated -= blocksPerDamage;
            player.damage(damageAmount);
        }
        distanceAccumulated.put(uuid, accumulated);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!active) return;
        distanceAccumulated.remove(event.getPlayer().getUniqueId());
    }

    @Override
    protected void onDisable() {
        distanceAccumulated.clear();
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofDouble("walking-damage.blocks-per-damage", "Blocks per Damage", 50.0, 10.0, 500.0, 10.0),
                ConfigurableSetting.ofDouble("walking-damage.damage-amount", "Damage Amount", 1.0, 0.5, 10.0, 0.5)
        );
    }
}
