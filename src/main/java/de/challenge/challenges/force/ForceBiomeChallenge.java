package de.challenge.challenges.force;

import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ForceBiomeChallenge extends AbstractForceChallenge {

    private static final List<Biome> BIOMES = List.of(
            Biome.PLAINS, Biome.FOREST, Biome.DESERT, Biome.TAIGA,
            Biome.SWAMP, Biome.RIVER, Biome.BEACH, Biome.JUNGLE,
            Biome.SAVANNA, Biome.BADLANDS, Biome.SNOWY_PLAINS,
            Biome.BIRCH_FOREST, Biome.DARK_FOREST, Biome.FLOWER_FOREST,
            Biome.MEADOW, Biome.OCEAN, Biome.MUSHROOM_FIELDS
    );

    public ForceBiomeChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "force_biome"; }

    @Override
    public String getDisplayName() { return "Force Biome"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.MAP); }

    @Override
    public String getDescription() { return "Travel to a specific biome within time limit"; }

    @Override
    protected String getConfigPrefix() { return "force-biome"; }

    @Override
    protected String pickObjective() {
        return BIOMES.get(ThreadLocalRandom.current().nextInt(BIOMES.size())).name();
    }

    @Override
    protected boolean checkObjective(Player player) {
        Biome current = player.getLocation().getBlock().getBiome();
        return current.name().equals(currentObjective);
    }

    @Override
    protected String getObjectiveDisplayName() {
        return "Go to biome: " + currentObjective.replace("_", " ").toLowerCase();
    }

    @Override protected String getTaskMessageKey() { return "force-biome.task"; }
    @Override protected String getSuccessMessageKey() { return "force-biome.success"; }
    @Override protected String getFailMessageKey() { return "force-biome.fail"; }
}
