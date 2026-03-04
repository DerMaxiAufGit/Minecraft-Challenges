package de.challenge;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public abstract class Challenge implements Listener {

    protected final ChallengePlugin plugin;
    protected boolean active = false;
    protected boolean preservingState = false;

    public Challenge(ChallengePlugin plugin) {
        this.plugin = plugin;
    }

    public abstract String getId();
    public abstract String getDisplayName();
    public abstract ItemStack getIcon();
    public abstract String getDescription();
    public abstract ChallengeCategory getCategory();

    public void enable() {
        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        onEnable();
    }

    public void disable() {
        disable(false);
    }

    public void disable(boolean preserveState) {
        active = false;
        this.preservingState = preserveState;
        HandlerList.unregisterAll(this);
        onDisable();
        this.preservingState = false;
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public boolean isActive() {
        return active;
    }

    public List<ConfigurableSetting> getConfigurableSettings() {
        return Collections.emptyList();
    }
}
