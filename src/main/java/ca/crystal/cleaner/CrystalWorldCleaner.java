
package ca.crystal.cleaner;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
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

    private static final int MAP_SIZE = 7000;
    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;
    private static final int CHUNKS_PER_TICK = 1;

    private int preloadX;
    private int preloadZ;
    private int minChunk;
    private int maxChunk;
    private boolean preloadFinished;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        World world = Bukkit.getWorld("world");

        if (world != null) {
            startPreload(world);
        } else {
            getLogger().warning(
                    "Le monde 'world' est introuvable. Preload non demarre."
            );
        }

        startCleaner();
        getLogger().info("CrystalWorldCleaner active.");
    }

    private void startPreload(World world) {
        int radius = MAP_SIZE / 2;

        minChunk = (CENTER_X - radius) >> 4;
        maxChunk = (CENTER_X + radius) >> 4;

        int minZ = (CENTER_Z - radius) >> 4;
        int maxZ = (CENTER_Z + radius) >> 4;

        preloadX = minChunk;
        preloadZ = minZ;
        preloadFinished = false;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (preloadFinished) {
                    cancel();
                    return;
                }

                int chunksThisTick = 0;

                while (chunksThisTick < CHUNKS_PER_TICK) {
                    world.getChunkAt(preloadX, preloadZ);

                    preloadZ++;

                    if (preloadZ > maxZ) {
                        preloadZ = minZ;
                        preloadX++;
                    }

                    chunksThisTick++;

                    if (preloadX > maxChunk) {
                        preloadFinished = true;
                        getLogger().info(
                                "Preload des chunks termine pour le monde "
                                        + world.getName()
                        );
                        cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L);

        getLogger().info(
                "Preload commence pour une zone d'environ "
                        + MAP_SIZE + " x " + MAP_SIZE + " blocs."
        );
    }

    private void startCleaner() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Chunk chunk = queue.poll();

                if (chunk == null || !chunk.isLoaded()) {
                    return;
                }

                queued.remove(key(chunk));

                World world = chunk.getWorld();

                if (world.getEnvironment() != World.Environment.NORMAL) {
                    return;
                }

                new BukkitRunnable() {
                    private int y = world.getMinHeight();

                    @Override
                    public void run() {
                        if (!chunk.isLoaded()) {
                            cancel();
                            return;
                        }

                        int endY = Math.min(y + 8, world.getMaxHeight());

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

                        if (y >= world.getMaxHeight()) {
                            cancel();
                        }
                    }
                }.runTaskTimer(CrystalWorldCleaner.this, 1L, 1L);
            }
        }.runTaskTimer(this, 1L, 1L);
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
                || name.equals("LILY_OF_THE_VALLEY")
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
