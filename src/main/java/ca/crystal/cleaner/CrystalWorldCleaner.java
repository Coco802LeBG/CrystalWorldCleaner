
package ca.crystal.cleaner;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public final class CrystalWorldCleaner extends JavaPlugin implements Listener {

    private final Queue<Chunk> queue = new ArrayDeque<>();
    private final Set<String> queued = new HashSet<>();

    private long lastInteractionLog = 0L;
    private long interactions = 0L;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                Chunk chunk = queue.poll();

                if (chunk == null || !chunk.isLoaded()) {
                    return;
                }

                queued.remove(key(chunk));

                if (chunk.getWorld().getEnvironment()
                        != World.Environment.NORMAL) {
                    return;
                }

                new BukkitRunnable() {
                    private int y = chunk.getWorld().getMinHeight();

                    @Override
                    public void run() {
                        if (!chunk.isLoaded()) {
                            cancel();
                            return;
                        }

                        int maxY = chunk.getWorld().getMaxHeight();
                        int endY = Math.min(y + 8, maxY);

                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int yy = y; yy < endY; yy++) {
                                    Material material =
                                            chunk.getBlock(x, yy, z).getType();

                                    if (material == Material.CAVE_AIR) {
                                        chunk.getBlock(x, yy, z)
                                                .setType(Material.STONE, false);
                                    } else if (material == Material.WATER
                                            || material == Material.LAVA
                                            || isVegetation(material)) {
                                        chunk.getBlock(x, yy, z)
                                                .setType(Material.AIR, false);
                                    }
                                }
                            }
                        }

                        y = endY;

                        if (y >= maxY) {
                            cancel();
                        }
                    }
                }.runTaskTimer(CrystalWorldCleaner.this, 1L, 1L);
            }
        }.runTaskTimer(this, 1L, 1L);

        getLogger().info("CrystalWorldCleaner active.");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.getWorld().getEnvironment()
                != World.Environment.NORMAL) {
            return;
        }

        Chunk chunk = event.getChunk();
        String id = key(chunk);

        if (queued.add(id)) {
            queue.offer(chunk);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (item.isValid() && !item.isDead()) {
                item.remove();
            }
        }, 20L);
    }

    @EventHandler
    public void onCrystalPvPInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Material item = event.getMaterial();

        if (item != Material.GLOWSTONE
                && item != Material.RESPAWN_ANCHOR) {
            return;
        }

        interactions++;

        long now = System.currentTimeMillis();

        if (now - lastInteractionLog >= 10000L) {
            getLogger().fine(
                    "Interactions anchor/glowstone sur 10 secondes : "
                            + interactions
            );

            interactions = 0L;
            lastInteractionLog = now;
        }
    }

    private String key(Chunk chunk) {
        return chunk.getWorld().getName()
                + ":" + chunk.getX()
                + ":" + chunk.getZ();
    }

    private boolean isVegetation(Material material) {
        String name = material.name();

        return name.endsWith("_LEAVES")
                || name.endsWith("_LOG")
                || name.endsWith("_WOOD")
                || name.endsWith("_SAPLING")
                || name.endsWith("_FLOWERS")
                || name.endsWith("_TULIP")
                || name.endsWith("_ORCHID")
                || name.endsWith("_CORAL")
                || name.endsWith("_CORAL_FAN")
                || name.equals("SHORT_GRASS")
                || name.equals("TALL_GRASS")
                || name.equals("GRASS")
                || name.equals("FERN")
                || name.equals("LARGE_FERN")
                || name.equals("DEAD_BUSH")
                || name.equals("VINE")
                || name.equals("GLOW_LICHEN")
                || name.equals("HANGING_ROOTS")
                || name.equals("MOSS_CARPET")
                || name.equals("MOSS_BLOCK")
                || name.equals("AZALEA")
                || name.equals("FLOWERING_AZALEA")
                || name.equals("SUGAR_CANE")
                || name.equals("CACTUS")
                || name.equals("BAMBOO")
                || name.equals("KELP")
                || name.equals("KELP_PLANT")
                || name.equals("SEAGRASS")
                || name.equals("TALL_SEAGRASS")
                || name.equals("LILY_PAD")
                || name.equals("PINK_PETALS")
                || name.equals("SPORE_BLOSSOM")
                || name.equals("TORCHFLOWER")
                || name.equals("PITCHER_PLANT")
                || name.equals("WHEAT")
                || name.equals("CARROTS")
                || name.equals("POTATOES")
                || name.equals("BEETROOTS")
                || name.equals("COCOA")
                || name.equals("MELON_STEM")
                || name.equals("PUMPKIN_STEM")
                || name.equals("BROWN_MUSHROOM")
                || name.equals("RED_MUSHROOM")
                || name.equals("BROWN_MUSHROOM_BLOCK")
                || name.equals("RED_MUSHROOM_BLOCK")
                || name.equals("MUSHROOM_STEM");
    }
}
