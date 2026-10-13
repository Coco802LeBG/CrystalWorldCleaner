package ca.crystal.cleaner;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public final class CrystalWorldCleaner extends JavaPlugin implements Listener {
    private final Queue<Chunk> queue = new ArrayDeque<>();
    private final Set<String> queued = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                Chunk chunk = queue.poll();
                if (chunk == null) return;

                String id = key(chunk);
                queued.remove(id);

                if (!chunk.isLoaded()) return;

                World world = chunk.getWorld();
                if (world.getEnvironment() != World.Environment.NORMAL) return;

                int minY = world.getMinHeight();
                int maxY = world.getMaxHeight();

                // Process a small vertical slice per tick to limit server stalls.
                int start = minY;
                while (start < maxY) {
                    int end = Math.min(start + 8, maxY);
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = start; y < end; y++) {
                                Material m = chunk.getBlock(x, y, z).getType();

                                if (m == Material.CAVE_AIR) {
                                    chunk.getBlock(x, y, z).setType(Material.STONE, false);
                                } else if (m == Material.WATER || m == Material.LAVA) {
                                    chunk.getBlock(x, y, z).setType(Material.AIR, false);
                                } else if (isVegetation(m)) {
                                    chunk.getBlock(x, y, z).setType(Material.AIR, false);
                                }
                            }
                        }
                    }
                    start = end;
                    // Continue the rest of this chunk on the next tick.
                    if (start < maxY) {
                        final int nextY = start;
                        new BukkitRunnable() {
                            int y = nextY;
                            @Override
                            public void run() {
                                if (!chunk.isLoaded()) {
                                    cancel();
                                    return;
                                }
                                int endY = Math.min(y + 8, maxY);
                                for (int x = 0; x < 16; x++) {
                                    for (int z = 0; z < 16; z++) {
                                        for (int yy = y; yy < endY; yy++) {
                                            Material m = chunk.getBlock(x, yy, z).getType();
                                            if (m == Material.CAVE_AIR) {
                                                chunk.getBlock(x, yy, z).setType(Material.STONE, false);
                                            } else if (m == Material.WATER || m == Material.LAVA || isVegetation(m)) {
                                                chunk.getBlock(x, yy, z).setType(Material.AIR, false);
                                            }
                                        }
                                    }
                                }
                                y = endY;
                                if (y >= maxY) cancel();
                            }
                        }.runTaskTimer(CrystalWorldCleaner.this, 1L, 1L);
                        return;
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L);

        getLogger().info("Crystal World Cleaner active.");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.getWorld().getEnvironment() != World.Environment.NORMAL) return;
        Chunk c = event.getChunk();
        String id = key(c);
        if (queued.add(id)) queue.offer(c);
    }

    private String key(Chunk c) {
        return c.getWorld().getName() + ":" + c.getX() + ":" + c.getZ();
    }

    private boolean isVegetation(Material m) {
        String n = m.name();
        return n.endsWith("_LEAVES") || n.endsWith("_LOG") ||
               n.endsWith("_WOOD") || n.endsWith("_SAPLING") ||
               n.endsWith("_FLOWERS") || n.endsWith("_TULIP") ||
               n.endsWith("_ORCHID") || n.endsWith("_MUSHROOM") ||
               n.endsWith("_CORAL") || n.endsWith("_CORAL_FAN") ||
               n.endsWith("_BANNER") && false ||
               n.equals("SHORT_GRASS") || n.equals("TALL_GRASS") ||
               n.equals("FERN") || n.equals("LARGE_FERN") ||
               n.equals("DEAD_BUSH") || n.equals("VINE") ||
               n.equals("GLOW_LICHEN") || n.equals("HANGING_ROOTS") ||
               n.equals("MOSS_CARPET") || n.equals("MOSS_BLOCK") ||
               n.equals("AZALEA") || n.equals("FLOWERING_AZALEA") ||
               n.equals("SUGAR_CANE") || n.equals("CACTUS") ||
               n.equals("BAMBOO") || n.equals("KELP") ||
               n.equals("KELP_PLANT") || n.equals("SEAGRASS") ||
               n.equals("TALL_SEAGRASS") || n.equals("LILY_PAD") ||
               n.equals("LILY_OF_THE_VALLEY") ||
               n.equals("PINK_PETALS") || n.equals("SPORE_BLOSSOM") ||
               n.equals("TORCHFLOWER") || n.equals("PITCHER_PLANT") ||
               n.equals("WHEAT") || n.equals("CARROTS") ||
               n.equals("POTATOES") || n.equals("BEETROOTS") ||
               n.equals("COCOA") || n.equals("MELON_STEM") ||
               n.equals("PUMPKIN_STEM") || n.equals("BROWN_MUSHROOM") ||
               n.equals("RED_MUSHROOM") || n.equals("BROWN_MUSHROOM_BLOCK") ||
               n.equals("RED_MUSHROOM_BLOCK") || n.equals("MUSHROOM_STEM");
    }
}
