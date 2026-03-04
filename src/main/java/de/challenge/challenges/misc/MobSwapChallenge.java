package de.challenge.challenges.misc;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class MobSwapChallenge extends Challenge {

    public MobSwapChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "mob_swap"; }

    @Override
    public String getDisplayName() { return "Mob Swap"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.ENDER_PEARL); }

    @Override
    public String getDescription() { return "Hitting a mob swaps your positions"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.MOBS; }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!active) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        if (mob instanceof Player) return; // Don't swap with other players

        Location playerLoc = player.getLocation().clone();
        Location mobLoc = mob.getLocation().clone();

        // Schedule swap for next tick to avoid issues during damage event
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isDead() && mob.isValid() && !mob.isDead()) {
                player.teleport(mobLoc);
                mob.teleport(playerLoc);
            }
        });
    }
}
