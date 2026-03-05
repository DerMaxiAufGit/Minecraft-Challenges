package de.challenge.challenges.environmental;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BedrockWallChallenge extends Challenge {

    private BukkitTask task;
    private int wallPosition;
    private String direction;
    private int lastBuiltWallPosition;
    private final Set<Long> builtColumns = new HashSet<>();

    public BedrockWallChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override public String getId() { return "bedrock_wall"; }
    @Override public String getDisplayName() { return "Bedrock Wall"; }
    @Override public ItemStack getIcon() { return new ItemStack(Material.BEDROCK); }
    @Override public String getDescription() { return "A bedrock wall chases you from one direction"; }
    @Override public ChallengeCategory getCategory() { return ChallengeCategory.ENVIRONMENTAL; }

    @Override
    protected void onEnable() {
        int speed = plugin.getSettingsManager().getInt("bedrock-wall.speed", 60);
        direction = plugin.getSettingsManager().getString("bedrock-wall.direction", "north");
        if (direction == null) direction = "north";

        Player firstPlayer = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (firstPlayer == null) {
            // Defer start until a player is online
            task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                Player p = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
                if (p != null) {
                    task.cancel();
                    initWall(p, speed);
                }
            }, 20L, 20L);
            return;
        }

        initWall(firstPlayer, speed);
    }

    private void initWall(Player firstPlayer, int speed) {
        wallPosition = switch (direction) {
            case "north" -> firstPlayer.getLocation().getBlockZ() + 50;
            case "south" -> firstPlayer.getLocation().getBlockZ() - 50;
            case "east" -> firstPlayer.getLocation().getBlockX() - 50;
            case "west" -> firstPlayer.getLocation().getBlockX() + 50;
            default -> firstPlayer.getLocation().getBlockZ() + 50;
        };
        lastBuiltWallPosition = wallPosition;
        builtColumns.clear();

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::advanceWall, 0L, speed);
    }

    private void advanceWall() {
        switch (direction) {
            case "north" -> wallPosition--;
            case "south" -> wallPosition++;
            case "east" -> wallPosition++;
            case "west" -> wallPosition--;
        }

        boolean isZ = direction.equals("north") || direction.equals("south");

        // Only build the new wall line (not rebuild all previous ones)
        for (Player player : Bukkit.getOnlinePlayers()) {
            World world = player.getWorld();
            int minY = world.getMinHeight();
            int maxBuildY = Math.min(world.getMaxHeight(), minY + 128);
            int playerSecondary = isZ ? player.getLocation().getBlockX() : player.getLocation().getBlockZ();

            // Build columns near this player for the current wall position only
            for (int secondary = playerSecondary - 10; secondary <= playerSecondary + 10; secondary++) {
                long colKey = ((long) wallPosition << 32) | (secondary & 0xFFFFFFFFL);
                if (!builtColumns.add(colKey)) continue; // already built this column

                // Spread block writes across ticks: build one column per tick-batch
                int bx = isZ ? secondary : wallPosition;
                int bz = isZ ? wallPosition : secondary;
                for (int y = minY; y < maxBuildY; y++) {
                    world.getBlockAt(bx, y, bz).setType(Material.BEDROCK);
                }
            }
        }
    }

    @Override
    protected void onDisable() {
        if (task != null) { task.cancel(); task = null; }
        builtColumns.clear();
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("bedrock-wall.speed", "Speed (ticks)", 60, 20, 200, 10),
                ConfigurableSetting.ofString("bedrock-wall.direction", "Direction", "north")
        );
    }
}
