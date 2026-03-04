package de.challenge;

import de.challenge.challenges.damage.*;
import de.challenge.challenges.floor.*;
import de.challenge.challenges.force.*;
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

public class ChallengePlugin extends JavaPlugin implements Listener {

    private static ChallengePlugin instance;
    private ChallengeManager challengeManager;
    private TimerManager timerManager;
    private MessageUtil messageUtil;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml", false);

        messageUtil = new MessageUtil(this);
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

        getLogger().info("ChallengePlugin enabled.");
    }

    @Override
    public void onDisable() {
        challengeManager.saveState();
        challengeManager.stopAll();
        timerManager.stop();
        timerManager.hideFromAll();
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("ChallengePlugin disabled.");
    }

    private void registerChallenges() {
        // Restrictions
        challengeManager.registerChallenge(new NoCraftingChallenge(this));
        challengeManager.registerChallenge(new NoXPChallenge(this));
        challengeManager.registerChallenge(new NoPlaceChallenge(this));
        challengeManager.registerChallenge(new NoBreakChallenge(this));
        challengeManager.registerChallenge(new NoTradingChallenge(this));

        // Movement
        challengeManager.registerChallenge(new NoSneakChallenge(this));
        challengeManager.registerChallenge(new NoJumpChallenge(this));
        challengeManager.registerChallenge(new AlwaysRunningChallenge(this));
        challengeManager.registerChallenge(new TrafficLightChallenge(this));

        // Damage
        challengeManager.registerChallenge(new ReversedDamageChallenge(this));
        challengeManager.registerChallenge(new AchievementDamageChallenge(this));
        challengeManager.registerChallenge(new RandomPotionOnDamageChallenge(this));

        // Floor
        challengeManager.registerChallenge(new FloorIsLavaChallenge(this));
        challengeManager.registerChallenge(new IceFloorChallenge(this));
        challengeManager.registerChallenge(new BlockDisappearChallenge(this));

        // Misc
        challengeManager.registerChallenge(new SnakeChallenge(this));
        challengeManager.registerChallenge(new AnvilRainChallenge(this));
        challengeManager.registerChallenge(new RandomMLGChallenge(this));
        challengeManager.registerChallenge(new OneDurabilityChallenge(this));
        challengeManager.registerChallenge(new DoubleSpawnChallenge(this));
        challengeManager.registerChallenge(new CoronaChallenge(this));

        // Randomizers
        challengeManager.registerChallenge(new BlockRandomizerChallenge(this));
        challengeManager.registerChallenge(new CraftingRandomizerChallenge(this));
        challengeManager.registerChallenge(new MobDropRandomizerChallenge(this));

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

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public static ChallengePlugin getInstance() {
        return instance;
    }
}
