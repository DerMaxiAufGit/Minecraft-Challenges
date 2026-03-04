package de.challenge;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TimerManager {

    private final ChallengePlugin plugin;
    private final BossBar bossBar;
    private BukkitTask task;
    private long elapsedSeconds = 0;
    private boolean running = false;
    private boolean countDown = false;
    private long startValue = 0;

    public TimerManager(ChallengePlugin plugin) {
        this.plugin = plugin;
        this.countDown = plugin.getConfig().getString("timer-direction", "up").equalsIgnoreCase("down");
        this.startValue = parseTime(plugin.getConfig().getString("timer-start-value", "00:00:00"));
        if (countDown) {
            this.elapsedSeconds = startValue;
        }
        this.bossBar = BossBar.bossBar(
                Component.text(formatTime(elapsedSeconds), NamedTextColor.WHITE),
                1.0f,
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS
        );
    }

    public void start() {
        if (running) return;
        running = true;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 20L);
        showToAll();
    }

    public void pause() {
        running = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void resume() {
        if (running) return;
        start();
    }

    public void reset() {
        pause();
        elapsedSeconds = countDown ? startValue : 0;
        updateBar();
    }

    public void setTime(long seconds) {
        this.elapsedSeconds = seconds;
        updateBar();
    }

    public void stop() {
        pause();
    }

    private void tick() {
        if (countDown) {
            if (elapsedSeconds > 0) {
                elapsedSeconds--;
            }
        } else {
            elapsedSeconds++;
        }
        updateBar();
    }

    private void updateBar() {
        String activeChallenges = plugin.getChallengeManager().getActiveChallengeNames();
        String timeStr = formatTime(elapsedSeconds);
        String barText = activeChallenges.isEmpty()
                ? "\u23F1 " + timeStr
                : "\u23F1 " + timeStr + " | " + activeChallenges;

        BossBar.Color color = BossBar.Color.GREEN;
        if (elapsedSeconds >= 7200) {
            color = BossBar.Color.RED;
        } else if (elapsedSeconds >= 3600) {
            color = BossBar.Color.YELLOW;
        }

        bossBar.name(Component.text(barText, NamedTextColor.WHITE));
        bossBar.color(color);

        if (countDown && startValue > 0) {
            bossBar.progress(Math.max(0f, Math.min(1f, (float) elapsedSeconds / startValue)));
        } else {
            bossBar.progress(1.0f);
        }
    }

    public void showToAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showBossBar(bossBar);
        }
    }

    public void addPlayer(Player player) {
        player.showBossBar(bossBar);
    }

    public void hideFromAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bossBar);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public String getFormattedTime() {
        return formatTime(elapsedSeconds);
    }

    public void loadState(long seconds, boolean wasRunning) {
        this.elapsedSeconds = seconds;
        updateBar();
        if (wasRunning) {
            start();
        }
    }

    public static String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long parseTime(String time) {
        String[] parts = time.split(":");
        if (parts.length != 3) return 0;
        try {
            return Long.parseLong(parts[0]) * 3600
                    + Long.parseLong(parts[1]) * 60
                    + Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
