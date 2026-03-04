package de.challenge.challenges.randomizer;

import de.challenge.Challenge;
import de.challenge.ChallengeCategory;
import de.challenge.ChallengePlugin;
import de.challenge.util.RandomizerMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CraftingRandomizerChallenge extends Challenge {

    private RandomizerMap<Material, Material> craftMap;

    public CraftingRandomizerChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "crafting_randomizer"; }

    @Override
    public String getDisplayName() { return "Crafting Randomizer"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.CRAFTING_TABLE); }

    @Override
    public String getDescription() { return "Crafting results are randomized"; }

    @Override
    public ChallengeCategory getCategory() { return ChallengeCategory.RANDOMIZER; }

    @Override
    protected void onEnable() {
        RandomizerMap.KeySerializer<Material> serializer = new RandomizerMap.KeySerializer<>() {
            @Override public String serialize(Material key) { return key.name(); }
            @Override public Material deserialize(String raw) { return Material.valueOf(raw); }
        };
        RandomizerMap.ValueSerializer<Material> valueSerializer = new RandomizerMap.ValueSerializer<>() {
            @Override public String serialize(Material value) { return value.name(); }
            @Override public Material deserialize(String raw) { return Material.valueOf(raw); }
        };

        craftMap = new RandomizerMap<>(plugin, "randomizer_crafting.yml", serializer, valueSerializer);
        craftMap.load();

        if (craftMap.isEmpty()) {
            Set<Material> craftable = new HashSet<>();
            Iterator<Recipe> iter = Bukkit.recipeIterator();
            while (iter.hasNext()) {
                Recipe recipe = iter.next();
                Material result = recipe.getResult().getType();
                if (!result.isAir()) {
                    craftable.add(result);
                }
            }

            List<Material> allItems = new ArrayList<>();
            for (Material mat : Material.values()) {
                if (mat.isItem() && !mat.isAir() && !mat.name().startsWith("LEGACY_")) {
                    allItems.add(mat);
                }
            }

            craftMap.generate(new ArrayList<>(craftable), allItems);
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!active) return;
        ItemStack result = event.getInventory().getResult();
        if (result == null || result.getType().isAir()) return;

        Material mapped = craftMap.get(result.getType());
        if (mapped != null) {
            event.getInventory().setResult(new ItemStack(mapped, result.getAmount()));
        }
    }

    @EventHandler
    public void onSmelt(FurnaceSmeltEvent event) {
        if (!active) return;
        Material resultType = event.getResult().getType();
        Material mapped = craftMap.get(resultType);
        if (mapped != null) {
            event.setResult(new ItemStack(mapped, event.getResult().getAmount()));
        }
    }

    @Override
    protected void onDisable() {
        if (craftMap != null) {
            if (!preservingState) {
                craftMap.delete();
            }
            craftMap = null;
        }
    }
}
