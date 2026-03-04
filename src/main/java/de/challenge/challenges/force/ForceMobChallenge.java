package de.challenge.challenges.force;

import de.challenge.ChallengePlugin;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ForceMobChallenge extends AbstractForceChallenge {

    private static final List<EntityType> MOBS = List.of(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
            EntityType.CREEPER, EntityType.ENDERMAN, EntityType.WITCH,
            EntityType.BLAZE, EntityType.GHAST, EntityType.SLIME,
            EntityType.PHANTOM, EntityType.DROWNED, EntityType.HUSK,
            EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.PIGLIN,
            EntityType.HOGLIN, EntityType.ZOMBIFIED_PIGLIN, EntityType.MAGMA_CUBE,
            EntityType.SILVERFISH, EntityType.CAVE_SPIDER, EntityType.COW,
            EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN
    );

    private boolean objectiveMet = false;

    public ForceMobChallenge(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "force_mob"; }

    @Override
    public String getDisplayName() { return "Force Mob"; }

    @Override
    public ItemStack getIcon() { return new ItemStack(Material.IRON_SWORD); }

    @Override
    public String getDescription() { return "Kill a specific mob within time limit"; }

    @Override
    protected String getConfigPrefix() { return "force-mob"; }

    @Override
    protected String pickObjective() {
        objectiveMet = false;
        return MOBS.get(ThreadLocalRandom.current().nextInt(MOBS.size())).name();
    }

    @Override
    protected boolean checkObjective(Player player) {
        return objectiveMet;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!active || !objectiveActive || currentObjective == null) return;
        if (event.getEntity().getKiller() == null) return;
        if (event.getEntity().getType().name().equals(currentObjective)) {
            objectiveMet = true;
        }
    }

    @Override
    protected String getObjectiveDisplayName() {
        return "Kill: " + currentObjective.replace("_", " ").toLowerCase();
    }

    @Override protected String getTaskMessageKey() { return "force-mob.task"; }
    @Override protected String getSuccessMessageKey() { return "force-mob.success"; }
    @Override protected String getFailMessageKey() { return "force-mob.fail"; }
}
