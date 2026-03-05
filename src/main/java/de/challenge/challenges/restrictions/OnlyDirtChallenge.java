package de.challenge.challenges.restrictions;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;

public class OnlyDirtChallenge extends Challenge {

    private static final Set<Material> DIRT_FAMILY = Set.of(
            Material.DIRT, Material.GRASS_BLOCK, Material.DIRT_PATH,
            Material.COARSE_DIRT, Material.ROOTED_DIRT, Material.PODZOL,
            Material.MYCELIUM, Material.FARMLAND, Material.MUD,
            Material.MUDDY_MANGROVE_ROOTS
    );

    private BukkitTask task;

    public OnlyDirtChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "only_dirt"; }
    @Override public String getDisplayName() { return "Only Dirt"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.DIRT); }
    @Override public String getDescription() { return "You must always stand on dirt or die"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.RESTRICTIONS; }

    @Override
    protected void onEnable() {
        int interval = plugin.getSettingsManager().getInt("only-dirt.check-interval-ticks", 20);
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                if (!player.isOnGround()) continue;
                Material below = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
                if (!DIRT_FAMILY.contains(below)) {
                    player.setHealth(0);
                    player.sendMessage(Component.text("You weren't standing on dirt!", NamedTextColor.RED));
                }
            }
        }, 40L, interval);
    }

    @Override
    protected void onDisable() {
        if (task != null) { task.cancel(); task = null; }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("only-dirt.check-interval-ticks", "Check Interval (ticks)", 20, 5, 100, 5)
        );
    }
}
