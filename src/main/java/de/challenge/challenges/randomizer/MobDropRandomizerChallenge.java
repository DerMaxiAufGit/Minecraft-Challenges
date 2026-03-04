package de.challenge.challenges.randomizer;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.util.RandomizerMap;
import org.bukkit.Material;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MobDropRandomizerChallenge extends Challenge {

    private RandomizerMap<EntityType, Material> mobMap;

    public MobDropRandomizerChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "mob_drop_randomizer"; }

    @Override
    public String getDisplayName() { return "Mob Drop Randomizer"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.ROTTEN_FLESH); }

    @Override
    public String getDescription() { return "Mob drops are randomized"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.RANDOMIZER; }

    @Override
    protected void onEnable() {
        RandomizerMap.KeySerializer<EntityType> keySerializer = new RandomizerMap.KeySerializer<>() {
            @Override public String serialize(EntityType key) { return key.name(); }
            @Override public EntityType deserialize(String raw) { return EntityType.valueOf(raw); }
        };
        RandomizerMap.ValueSerializer<Material> valueSerializer = new RandomizerMap.ValueSerializer<>() {
            @Override public String serialize(Material value) { return value.name(); }
            @Override public Material deserialize(String raw) { return Material.valueOf(raw); }
        };

        mobMap = new RandomizerMap<>(plugin, "randomizer_mobs.yml", keySerializer, valueSerializer);
        mobMap.load();

        if (mobMap.isEmpty()) {
            List<EntityType> mobTypes = new ArrayList<>();
            for (EntityType type : EntityType.values()) {
                if (type.isAlive() && type != EntityType.PLAYER && type != EntityType.ENDER_DRAGON) {
                    mobTypes.add(type);
                }
            }

            List<Material> allItems = new ArrayList<>();
            for (Material mat : Material.values()) {
                if (mat.isItem() && !mat.isAir() && !mat.name().startsWith("LEGACY_")) {
                    allItems.add(mat);
                }
            }

            mobMap.generate(mobTypes, allItems);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!active) return;
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player || entity instanceof EnderDragon) return;

        Material mapped = mobMap.get(entity.getType());
        if (mapped != null) {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(mapped));
        }
    }

    @Override
    protected void onDisable() {
        if (mobMap != null) {
            if (!preservingState) {
                mobMap.delete();
            }
            mobMap = null;
        }
    }
}
