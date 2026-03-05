package de.challenge.challenges.damage;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class DelayedDamageChallenge extends Challenge {

    private final List<BukkitTask> pendingTasks = new ArrayList<>();

    public DelayedDamageChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "delayed_damage"; }
    @Override public String getDisplayName() { return "Delayed Damage"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.CLOCK); }
    @Override public String getDescription() { return "All damage is delayed"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.DAMAGE; }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent event) {
        if (!active) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM) return;

        double damage = event.getDamage();
        event.setCancelled(true);

        int delaySeconds = plugin.getSettingsManager().getInt("delayed-damage.delay-seconds", 5);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!active || !player.isOnline() || player.isDead()) return;
            player.damage(damage);
        }, delaySeconds * 20L);
        pendingTasks.add(task);
    }

    @Override
    protected void onDisable() {
        for (BukkitTask task : pendingTasks) {
            task.cancel();
        }
        pendingTasks.clear();
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("delayed-damage.delay-seconds", "Delay (s)", 5, 1, 30, 1)
        );
    }
}
