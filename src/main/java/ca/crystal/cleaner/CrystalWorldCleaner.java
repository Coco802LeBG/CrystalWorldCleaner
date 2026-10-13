
package ca.crystal.cleaner;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrystalWorldCleaner extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("CrystalWorldCleaner enabled.");
    }

    /*
     * ITEM DROP CLEANER
     * Removes dropped items after approximately 1 second.
     */
    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (item.isValid() && !item.isDead()) {
                item.remove();
            }
        }, 20L);
    }

    /*
     * ANCHOR / GLOWSTONE INTERACTION OPTIMIZER
     * Keeps vanilla interactions intact.
     * Does not cancel events or change game mechanics.
     */
    @EventHandler
    public void onAnchorInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Material material = event.getMaterial();

        if (material != Material.RESPAWN_ANCHOR
                && material != Material.GLOWSTONE) {
            return;
        }

        // Intentionally no delay, event cancellation, or extra task.
        // Vanilla handles the interaction normally.
    }
}
