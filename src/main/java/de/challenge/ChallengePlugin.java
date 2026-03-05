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
import de.challenge.challenges.modifiers.*;
import de.challenge.commands.BackpackCommand;
import de.challenge.commands.ChallengeCommand;
import de.challenge.commands.ResetCommand;
import de.challenge.commands.TimerCommand;
import de.challenge.commands.VillageCommand;
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
        getCommand("backpack").setExecutor(new BackpackCommand(this));
        getCommand("village").setExecutor(new VillageCommand(this));

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

        instance = null;
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
        challengeManager.registerChallenge(new NoFoodChallenge(this));
        challengeManager.registerChallenge(new NoLightChallenge(this));
        challengeManager.registerChallenge(new NoFallDamageChallenge(this));
        challengeManager.registerChallenge(new NoDuplicateItemsChallenge(this));
        challengeManager.registerChallenge(new OnlyDirtChallenge(this));
        challengeManager.registerChallenge(new OnlyEnderpearlsChallenge(this));
        challengeManager.registerChallenge(new OnlyMinecartChallenge(this));

        // Movement
        challengeManager.registerChallenge(new NoSneakChallenge(this));
        challengeManager.registerChallenge(new NoJumpChallenge(this));
        challengeManager.registerChallenge(new AlwaysRunningChallenge(this));
        challengeManager.registerChallenge(new TrafficLightChallenge(this));
        challengeManager.registerChallenge(new NoWASDChallenge(this));
        challengeManager.registerChallenge(new OnlyUpwardChallenge(this));
        challengeManager.registerChallenge(new OnlyDownwardChallenge(this));
        challengeManager.registerChallenge(new SocialDistancingChallenge(this));
        challengeManager.registerChallenge(new SpeedChallenge(this));

        // Damage
        challengeManager.registerChallenge(new ReversedDamageChallenge(this));
        challengeManager.registerChallenge(new AchievementDamageChallenge(this));
        challengeManager.registerChallenge(new RandomPotionOnDamageChallenge(this));
        challengeManager.registerChallenge(new SharedHealthChallenge(this));
        challengeManager.registerChallenge(new NeverFullHeartsChallenge(this));
        challengeManager.registerChallenge(new DamageFreezeChallenge(this));
        challengeManager.registerChallenge(new HalfHeartChallenge(this));
        challengeManager.registerChallenge(new DamageLaunchChallenge(this));
        challengeManager.registerChallenge(new KillEffectChallenge(this));
        challengeManager.registerChallenge(new MobDamageEffectChallenge(this));
        challengeManager.registerChallenge(new NoEqualHeartsChallenge(this));
        challengeManager.registerChallenge(new ItemDamageChallenge(this));
        challengeManager.registerChallenge(new RandomizedHPChallenge(this));
        challengeManager.registerChallenge(new DelayedDamageChallenge(this));
        challengeManager.registerChallenge(new WalkingDamageChallenge(this));

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
        challengeManager.registerChallenge(new TNTRunChallenge(this));
        challengeManager.registerChallenge(new MinedBlocksChunkMinedChallenge(this));
        challengeManager.registerChallenge(new ChunkDecayChallenge(this));
        challengeManager.registerChallenge(new EverythingReversedChallenge(this));
        challengeManager.registerChallenge(new FloorHoleChallenge(this));
        challengeManager.registerChallenge(new ChunkEffectChallenge(this));
        challengeManager.registerChallenge(new ChunkBlockRandomizerChallenge(this));
        challengeManager.registerChallenge(new BedrockWallChallenge(this));

        // Floor (misc)
        challengeManager.registerChallenge(new SnakeChallenge(this));

        // Randomizers
        challengeManager.registerChallenge(new BlockRandomizerChallenge(this));
        challengeManager.registerChallenge(new CraftingRandomizerChallenge(this));
        challengeManager.registerChallenge(new MobDropRandomizerChallenge(this));
        challengeManager.registerChallenge(new BiomeEffectChallenge(this));
        challengeManager.registerChallenge(new SuperRandomizerChallenge(this));
        challengeManager.registerChallenge(new RandomHotbarChallenge(this));

        // Force
        challengeManager.registerChallenge(new ForceBlockChallenge(this));
        challengeManager.registerChallenge(new ForceMobChallenge(this));
        challengeManager.registerChallenge(new ForceItemChallenge(this));
        challengeManager.registerChallenge(new ForceBiomeChallenge(this));
        challengeManager.registerChallenge(new ForceHeightChallenge(this));
        challengeManager.registerChallenge(new ForceItemBattleChallenge(this));

        // Projects
        challengeManager.registerChallenge(new AllItemsProject(this));
        challengeManager.registerChallenge(new AllMobsProject(this));
        challengeManager.registerChallenge(new AllAdvancementsProject(this));
        challengeManager.registerChallenge(new AllDeathMessagesProject(this));
        challengeManager.registerChallenge(new AllSoundsProject(this));
        challengeManager.registerChallenge(new FullNetheritBeaconProject(this));

        // Modifiers
        challengeManager.registerChallenge(new GoalSelectionModifier(this));
        challengeManager.registerChallenge(new HealthSplitModifier(this));
        challengeManager.registerChallenge(new PvPToggleModifier(this));
        challengeManager.registerChallenge(new OldPvPModifier(this));
        challengeManager.registerChallenge(new DamageMultiplierModifier(this));
        challengeManager.registerChallenge(new CutCleanModifier(this));
        challengeManager.registerChallenge(new TimberModifier(this));
        challengeManager.registerChallenge(new SoupHealingModifier(this));
        challengeManager.registerChallenge(new NoHitDelayModifier(this));
        challengeManager.registerChallenge(new PlayerGlowModifier(this));
        challengeManager.registerChallenge(new BackpackModifier(this));
        challengeManager.registerChallenge(new RespawnModifier(this));
        challengeManager.registerChallenge(new VillageTeleporterModifier(this));
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

    private boolean deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) return true;
        boolean success = true;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    success &= deleteDirectory(f);
                } else if (!f.delete()) {
                    getLogger().warning("Could not delete file: " + f.getAbsolutePath());
                    success = false;
                }
            }
        }
        if (!dir.delete()) {
            getLogger().warning("Could not delete directory: " + dir.getAbsolutePath());
            success = false;
        }
        return success;
    }

    public static ChallengePlugin getInstance() {
        return instance;
    }
}
