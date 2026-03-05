package de.challenge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class ChallengeManager implements Listener {

    private final ChallengePlugin plugin;
    private final Map<String, Challenge> registeredChallenges = new LinkedHashMap<>();
    private final Set<String> activeChallengeIds = new LinkedHashSet<>();
    private boolean challengeRunning = false;
    private boolean challengeEnded = false;

    public ChallengeManager(ChallengePlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void registerChallenge(Challenge challenge) {
        registeredChallenges.put(challenge.getId(), challenge);
    }

    public void enableChallenge(String id) {
        Challenge challenge = registeredChallenges.get(id);
        if (challenge != null && !activeChallengeIds.contains(id)) {
            activeChallengeIds.add(id);
            if (challengeRunning) {
                challenge.enable();
            }
        }
    }

    public void disableChallenge(String id) {
        Challenge challenge = registeredChallenges.get(id);
        if (challenge != null && activeChallengeIds.remove(id)) {
            if (challenge.isActive()) {
                challenge.disable();
            }
        }
    }

    public void toggleChallenge(String id) {
        if (activeChallengeIds.contains(id)) {
            disableChallenge(id);
        } else {
            enableChallenge(id);
        }
    }

    public boolean isActive(String id) {
        return activeChallengeIds.contains(id);
    }

    public void startAll() {
        challengeRunning = true;
        challengeEnded = false;
        for (String id : activeChallengeIds) {
            Challenge c = registeredChallenges.get(id);
            if (c != null && !c.isActive()) {
                c.enable();
            }
        }
    }

    public void stopAll() {
        stopAll(false);
    }

    public void stopAll(boolean preserveState) {
        challengeRunning = false;
        for (Challenge c : registeredChallenges.values()) {
            if (c.isActive()) {
                c.disable(preserveState);
            }
        }
    }

    public void resetAll() {
        stopAll();
        activeChallengeIds.clear();
        challengeEnded = false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!challengeRunning || challengeEnded) return;
        String goal = plugin.getSettingsManager().getString("goal-selection.goal", "ENDER_DRAGON");
        if (goal == null || !event.getEntity().getType().name().equals(goal)) return;
        onChallengeWon();
    }

    public void triggerWin() {
        if (!challengeRunning || challengeEnded) return;
        onChallengeWon();
    }

    public void triggerLoss(Player player) {
        if (!challengeRunning || challengeEnded) return;
        onChallengeLost(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!challengeRunning || challengeEnded) return;
        if (!plugin.getSettingsManager().getBoolean("death-ends-challenge", true)) return;

        // Check if any active challenge overrides death (like AllDeathMessages project)
        for (String id : activeChallengeIds) {
            Challenge c = registeredChallenges.get(id);
            if (c instanceof DeathOverride override && override.allowDeath()) {
                return;
            }
        }
        onChallengeLost(event.getPlayer());
    }

    private void onChallengeWon() {
        challengeEnded = true;
        String time = plugin.getTimerManager().getFormattedTime();
        plugin.getTimerManager().stop();
        stopAll(true);

        Title title = Title.title(
                Component.text("Challenge Complete!", NamedTextColor.GREEN, TextDecoration.BOLD),
                Component.text("Time: " + time, NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofSeconds(2))
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }
    }

    private void onChallengeLost(Player deadPlayer) {
        challengeEnded = true;
        plugin.getTimerManager().stop();
        stopAll(true);

        Title title = Title.title(
                Component.text("Challenge Failed!", NamedTextColor.RED, TextDecoration.BOLD),
                Component.text(deadPlayer.getName() + " died.", NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofSeconds(2))
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1f, 1f);
        }
    }

    public String getActiveChallengeNames() {
        return activeChallengeIds.stream()
                .map(id -> {
                    Challenge c = registeredChallenges.get(id);
                    return c != null ? c.getDisplayName() : id;
                })
                .collect(Collectors.joining(", "));
    }

    public Map<String, Challenge> getRegisteredChallenges() {
        return Collections.unmodifiableMap(registeredChallenges);
    }

    public List<Challenge> getChallengesByCategory(ChallengeCategory category) {
        return registeredChallenges.values().stream()
                .filter(c -> c.getCategory() == category)
                .collect(Collectors.toList());
    }

    public long getActiveChallengeCountInCategory(ChallengeCategory category) {
        return registeredChallenges.values().stream()
                .filter(c -> c.getCategory() == category)
                .filter(c -> activeChallengeIds.contains(c.getId()))
                .count();
    }

    public Set<String> getActiveChallengeIds() {
        return Collections.unmodifiableSet(activeChallengeIds);
    }

    public boolean isChallengeRunning() {
        return challengeRunning;
    }

    public boolean isChallengeEnded() {
        return challengeEnded;
    }

    public void saveState() {
        File file = new File(plugin.getDataFolder(), "state.yml");
        FileConfiguration config = new YamlConfiguration();
        config.set("active-challenges", new ArrayList<>(activeChallengeIds));
        config.set("timer-seconds", plugin.getTimerManager().getElapsedSeconds());
        config.set("timer-running", plugin.getTimerManager().isRunning());
        config.set("challenge-running", challengeRunning);
        config.set("challenge-ended", challengeEnded);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save state: " + e.getMessage());
        }
    }

    public void loadState() {
        File file = new File(plugin.getDataFolder(), "state.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> active = config.getStringList("active-challenges");
        for (String id : active) {
            if (registeredChallenges.containsKey(id)) {
                activeChallengeIds.add(id);
            }
        }
        long timerSeconds = config.getLong("timer-seconds", 0);
        boolean timerRunning = config.getBoolean("timer-running", false);
        boolean wasRunning = config.getBoolean("challenge-running", false);
        challengeEnded = config.getBoolean("challenge-ended", false);

        plugin.getTimerManager().loadState(timerSeconds, timerRunning);
        if (wasRunning && !challengeEnded) {
            challengeRunning = true;
            startAll();
        }
    }
}
