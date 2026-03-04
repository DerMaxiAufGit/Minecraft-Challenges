package de.challenge.challenges.movement;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class TrafficLightChallenge extends Challenge {

    private enum Phase { GREEN, YELLOW, RED }

    private BukkitTask task;
    private BossBar trafficBar;
    private Phase currentPhase = Phase.GREEN;
    private int ticksInPhase = 0;
    private int greenTicks, yellowTicks, redTicks;

    public TrafficLightChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "traffic_light"; }

    @Override
    public String getDisplayName() { return "Traffic Light"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.REDSTONE_LAMP); }

    @Override
    public String getDescription() { return "Stop on red light or die"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.MOVEMENT; }

    @Override
    protected void onEnable() {
        greenTicks = plugin.getSettingsManager().getInt("traffic-light.green-seconds", 20) * 20;
        yellowTicks = plugin.getSettingsManager().getInt("traffic-light.yellow-seconds", 5) * 20;
        redTicks = plugin.getSettingsManager().getInt("traffic-light.red-seconds", 10) * 20;

        currentPhase = Phase.GREEN;
        ticksInPhase = 0;

        trafficBar = BossBar.bossBar(
                Component.text("GREEN - Go!", NamedTextColor.GREEN, TextDecoration.BOLD),
                1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS
        );

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showBossBar(trafficBar);
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ticksInPhase++;
            int phaseDuration = switch (currentPhase) {
                case GREEN -> greenTicks;
                case YELLOW -> yellowTicks;
                case RED -> redTicks;
            };

            float progress = Math.max(0f, 1f - (float) ticksInPhase / phaseDuration);
            trafficBar.progress(progress);

            if (ticksInPhase >= phaseDuration) {
                ticksInPhase = 0;
                currentPhase = switch (currentPhase) {
                    case GREEN -> Phase.YELLOW;
                    case YELLOW -> Phase.RED;
                    case RED -> Phase.GREEN;
                };
                updateBar();
            }
        }, 0L, 1L);
    }

    private void updateBar() {
        switch (currentPhase) {
            case GREEN -> {
                trafficBar.name(Component.text("GREEN - Go!", NamedTextColor.GREEN, TextDecoration.BOLD));
                trafficBar.color(BossBar.Color.GREEN);
            }
            case YELLOW -> {
                trafficBar.name(Component.text("YELLOW - Warning!", NamedTextColor.YELLOW, TextDecoration.BOLD));
                trafficBar.color(BossBar.Color.YELLOW);
            }
            case RED -> {
                trafficBar.name(Component.text("RED - STOP!", NamedTextColor.RED, TextDecoration.BOLD));
                trafficBar.color(BossBar.Color.RED);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!active || currentPhase != Phase.RED) return;
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            event.getPlayer().damage(event.getPlayer().getHealth() + 1);
        }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        return List.of(
                ConfigurableSetting.ofInt("traffic-light.green-seconds", "Green Duration (s)", 20, 5, 120, 5),
                ConfigurableSetting.ofInt("traffic-light.yellow-seconds", "Yellow Duration (s)", 5, 1, 30, 1),
                ConfigurableSetting.ofInt("traffic-light.red-seconds", "Red Duration (s)", 10, 3, 60, 5)
        );
    }

    @Override
    protected void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (trafficBar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hideBossBar(trafficBar);
            }
            trafficBar = null;
        }
    }
}
