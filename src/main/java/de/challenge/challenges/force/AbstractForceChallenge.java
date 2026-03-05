package de.challenge.challenges.force;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.ConfigurableSetting;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractForceChallenge extends Challenge {

    private BukkitTask intervalTask;
    private BukkitTask countdownTask;
    protected String currentObjective;
    protected int remainingSeconds;
    protected boolean objectiveActive = false;

    public AbstractForceChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.FORCE; }

    protected abstract String getConfigPrefix();
    protected abstract String pickObjective();
    protected abstract boolean checkObjective(Player player);
    protected abstract String getObjectiveDisplayName();
    protected abstract String getTaskMessageKey();
    protected abstract String getSuccessMessageKey();
    protected abstract String getFailMessageKey();

    @Override
    protected void onEnable() {
        int intervalSeconds = plugin.getSettingsManager().getInt(getConfigPrefix() + ".interval-seconds", 180);
        intervalTask = Bukkit.getScheduler().runTaskTimer(plugin, this::startNewObjective,
                intervalSeconds * 20L, intervalSeconds * 20L);
    }

    private void startNewObjective() {
        // Cancel any previous countdown still running
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        objectiveActive = false;

        currentObjective = pickObjective();
        if (currentObjective == null) return;

        int timeLimit = plugin.getSettingsManager().getInt(getConfigPrefix() + ".time-limit-seconds", 30);
        remainingSeconds = timeLimit;
        objectiveActive = true;

        // Show title to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Component.text(getObjectiveDisplayName(), NamedTextColor.YELLOW));
        }

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!objectiveActive) return;
            remainingSeconds--;

            // Check all players
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isDead()) continue;
                if (checkObjective(player)) {
                    onSuccess();
                    return;
                }
                // Action bar countdown
                player.sendActionBar(Component.text(
                        getObjectiveDisplayName() + " - " + remainingSeconds + "s remaining",
                        NamedTextColor.YELLOW));
            }

            if (remainingSeconds <= 0) {
                onFail();
            }
        }, 20L, 20L);
    }

    private void onSuccess() {
        objectiveActive = false;
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Component.text("Objective complete!", NamedTextColor.GREEN));
        }
    }

    private void onFail() {
        objectiveActive = false;
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        String punishment = plugin.getSettingsManager().getString(getConfigPrefix() + ".punishment", "damage");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Component.text("Time's up!", NamedTextColor.RED));
            switch (punishment) {
                case "kill" -> player.damage(player.getHealth() + 1);
                case "damage" -> player.damage(4.0);
                case "end" -> plugin.getChallengeManager().triggerLoss(player);
            }
        }
    }

    @Override
    public List<ConfigurableSetting> getConfigurableSettings() {
        String prefix = getConfigPrefix();
        return List.of(
                ConfigurableSetting.ofInt(prefix + ".interval-seconds", "Interval (s)", 180, 30, 900, 30),
                ConfigurableSetting.ofInt(prefix + ".time-limit-seconds", "Time Limit (s)", 30, 10, 300, 10),
                ConfigurableSetting.ofString(prefix + ".punishment", "Punishment", "damage")
        );
    }

    @Override
    protected void onDisable() {
        objectiveActive = false;
        if (intervalTask != null) {
            intervalTask.cancel();
            intervalTask = null;
        }
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }
}
