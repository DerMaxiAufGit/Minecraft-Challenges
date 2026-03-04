package de.challenge;

import de.challenge.challenges.damage.*;
import de.challenge.challenges.environmental.*;
import de.challenge.challenges.floor.*;
import de.challenge.challenges.force.*;
import de.challenge.challenges.inventory.*;
import de.challenge.challenges.misc.*;
import de.challenge.challenges.movement.*;
import de.challenge.challenges.projects.*;
import de.challenge.challenges.randomizer.*;
import de.challenge.challenges.restrictions.*;
import de.challenge.commands.ChallengeCommand;
import de.challenge.commands.ResetCommand;
import de.challenge.commands.TimerCommand;
import de.challenge.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChallengePlugin extends JavaPlugin implements Listener {

    private static ChallengePlugin instance;
    private ChallengeManager challengeManager;
    private ChallengeSettingsManager settingsManager;
    private TimerManager timerManager;
    private MessageUtil messageUtil;
    private List<File> worldFoldersToDelete = new ArrayList<>();

    @Override
    public void onLoad() {
        // onLoad() runs BEFORE worlds are loaded — delete world folders here if a reset is pending
        File marker = new File(getDataFolder(), "reset_pending");
        if (marker.exists()) {
            getLogger().info("Reset pending — deleting world folders before world load...");
            File serverRoot = getServer().getWorldContainer();
            for (String worldName : List.of("world", "world_nether", "world_the_end")) {
                File worldFolder = new File(serverRoot, worldName);
                if (worldFolder.exists()) {
                    deleteDirectory(worldFolder);
                    getLogger().info("Deleted world folder: " + worldName);
                }
            }
            marker.delete();
            getLogger().info("World reset complete. Fresh worlds will be generated.");
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml", false);

        messageUtil = new MessageUtil(this);
        settingsManager = new ChallengeSettingsManager(this);
        settingsManager.load();
        timerManager = new TimerManager(this);
        challengeManager = new ChallengeManager(this);

        registerChallenges();

        getCommand("challenge").setExecutor(new ChallengeCommand(this));
        getCommand("settings").setExecutor(new ChallengeCommand(this));
        TimerCommand timerCommand = new TimerCommand(this);
        getCommand("timer").setExecutor(timerCommand);
        getCommand("timer").setTabCompleter(timerCommand);
        getCommand("reset").setExecutor(new ResetCommand(this));

        Bukkit.getPluginManager().registerEvents(this, this);

        challengeManager.loadState();

        // Auto-save state every 5 minutes as crash safety net
        Bukkit.getScheduler().runTaskTimer(this, () -> challengeManager.saveState(), 6000L, 6000L);

        getLogger().info("ChallengePlugin enabled.");
    }

    @Override
    public void onDisable() {
        // Don't save state if we're resetting (world folders queued for deletion)
        if (worldFoldersToDelete.isEmpty()) {
            challengeManager.saveState();
            challengeManager.stopAll(true);  // preserve state files on normal shutdown
        } else {
            challengeManager.stopAll(false); // clean up files on reset
        }
        timerManager.stop();
        timerManager.hideFromAll();
        Bukkit.getScheduler().cancelTasks(this);

        // World folder deletion is handled by onLoad() on next startup via the reset_pending marker.
        // Attempting to delete here is unreliable because Bukkit re-saves worlds after onDisable().
        worldFoldersToDelete.clear();

        getLogger().info("ChallengePlugin disabled.");
    }

    private void registerChallenges() {
        // Restrictions
        challengeManager.registerChallenge(new NoCraftingChallenge(this));
        challengeManager.registerChallenge(new NoXPChallenge(this));
        challengeManager.registerChallenge(new NoPlaceChallenge(this));
        challengeManager.registerChallenge(new NoBreakChallenge(this));
        challengeManager.registerChallenge(new NoTradingChallenge(this));
        challengeManager.registerChallenge(new NoArmorChallenge(this));
        challengeManager.registerChallenge(new NoBedsChallenge(this));
        challengeManager.registerChallenge(new NoEnchantingChallenge(this));

        // Movement
        challengeManager.registerChallenge(new NoSneakChallenge(this));
        challengeManager.registerChallenge(new NoJumpChallenge(this));
        challengeManager.registerChallenge(new AlwaysRunningChallenge(this));
        challengeManager.registerChallenge(new TrafficLightChallenge(this));

        // Damage
        challengeManager.registerChallenge(new ReversedDamageChallenge(this));
        challengeManager.registerChallenge(new AchievementDamageChallenge(this));
        challengeManager.registerChallenge(new RandomPotionOnDamageChallenge(this));
        challengeManager.registerChallenge(new SharedHealthChallenge(this));
        challengeManager.registerChallenge(new NeverFullHeartsChallenge(this));
        challengeManager.registerChallenge(new DamageFreezeChallenge(this));

        // Floor
        challengeManager.registerChallenge(new FloorIsLavaChallenge(this));
        challengeManager.registerChallenge(new IceFloorChallenge(this));
        challengeManager.registerChallenge(new BlockDisappearChallenge(this));

        // Inventory
        challengeManager.registerChallenge(new OneDurabilityChallenge(this));
        challengeManager.registerChallenge(new DamageClearsInventoryChallenge(this));
        challengeManager.registerChallenge(new DietChallenge(this));
        challengeManager.registerChallenge(new NoDropChallenge(this));
        challengeManager.registerChallenge(new RandomItemDropChallenge(this));

        // Mobs
        challengeManager.registerChallenge(new DoubleSpawnChallenge(this));
        challengeManager.registerChallenge(new CoronaChallenge(this));
        challengeManager.registerChallenge(new ChunkMobChallenge(this));
        challengeManager.registerChallenge(new JumpMobSpawnChallenge(this));
        challengeManager.registerChallenge(new MobSwapChallenge(this));

        // Environmental
        challengeManager.registerChallenge(new AnvilRainChallenge(this));
        challengeManager.registerChallenge(new RandomMLGChallenge(this));
        challengeManager.registerChallenge(new ShrinkingBorderChallenge(this));
        challengeManager.registerChallenge(new LevelBorderChallenge(this));

        // Floor (misc)
        challengeManager.registerChallenge(new SnakeChallenge(this));

        // Randomizers
        challengeManager.registerChallenge(new BlockRandomizerChallenge(this));
        challengeManager.registerChallenge(new CraftingRandomizerChallenge(this));
        challengeManager.registerChallenge(new MobDropRandomizerChallenge(this));
        challengeManager.registerChallenge(new BiomeEffectChallenge(this));

        // Force
        challengeManager.registerChallenge(new ForceBlockChallenge(this));
        challengeManager.registerChallenge(new ForceMobChallenge(this));
        challengeManager.registerChallenge(new ForceItemChallenge(this));
        challengeManager.registerChallenge(new ForceBiomeChallenge(this));
        challengeManager.registerChallenge(new ForceHeightChallenge(this));

        // Projects
        challengeManager.registerChallenge(new AllItemsProject(this));
        challengeManager.registerChallenge(new AllMobsProject(this));
        challengeManager.registerChallenge(new AllAdvancementsProject(this));
        challengeManager.registerChallenge(new AllDeathMessagesProject(this));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (timerManager.isRunning()) {
            timerManager.addPlayer(event.getPlayer());
        }
    }

    public ChallengeManager getChallengeManager() {
        return challengeManager;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public ChallengeSettingsManager getSettingsManager() {
        return settingsManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public void setWorldFoldersToDelete(List<File> folders) {
        this.worldFoldersToDelete = new ArrayList<>(folders);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }

    public static ChallengePlugin getInstance() {
        return instance;
    }
}
