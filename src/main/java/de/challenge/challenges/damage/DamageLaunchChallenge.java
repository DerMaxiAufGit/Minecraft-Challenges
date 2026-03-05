package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class DamageLaunchChallenge extends Challenge {

    public DamageLaunchChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "damage_launch"; }
    @Override public String getDisplayName() { return "Damage Launch"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.FIREWORK_ROCKET); }
    @Override public String getDescription() { return "Taking damage launches you into the air"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player player)) return;
        double force = plugin.getSettingsManager().getDouble("damage-launch.vertical-force", 1.5);
        player.setVelocity(player.getVelocity().add(new Vector(0, force, 0)));
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofDouble("damage-launch.vertical-force", "Vertical Force", 1.5, 0.5, 5.0, 0.5)
        );
    }
}
