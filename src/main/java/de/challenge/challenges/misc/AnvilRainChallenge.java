package de.challenge.challenges.misc;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class AnvilRainChallenge extends Challenge {

    private BukkitTask task;

    public AnvilRainChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "anvil_rain"; }

    @Override
    public String getDisplayName() { return "Anvil Rain"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.ANVIL); }

    @Override
    public String getDescription() { return "Anvils periodically fall from the sky"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @Override
    protected void onEnable() {
        int interval = plugin.getConfig().getInt("anvil-rain.interval-ticks", 200);
        int height = plugin.getConfig().getInt("anvil-rain.height", 20);

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                Location loc = player.getLocation().add(0, height, 0);
                FallingBlock anvil = player.getWorld().spawnFallingBlock(loc,
                        Material.ANVIL.createBlockData());
                anvil.setDropItem(false);
                anvil.setHurtEntities(true);
            }
        }, interval, interval);
    }

    @Override
    protected void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
