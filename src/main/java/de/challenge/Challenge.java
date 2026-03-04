package de.challenge;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public abstract class Challenge implements Listener {

    protected final ChallengePlugin plugin;
    protected boolean active = false;

    public Challenge(ChallengePlugin plugin) {
        this.plugin = plugin;
    }

    public abstract String getId();
    public abstract String getDisplayName();
    public abstract ItemStack getIcon();
    public abstract String getDescription();

    public void enable() {
        active = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        onEnable();
    }

    public void disable() {
        active = false;
        HandlerList.unregisterAll(this);
        onDisable();
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public boolean isActive() {
        return active;
    }
}
