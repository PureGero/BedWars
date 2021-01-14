package net.justminecraft.minigames.bedwars;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class GlowStoneFixer implements Runnable {
    private final JavaPlugin plugin;
    private final World world;
    private final HashSet<String> doneChunks = new HashSet<>();
    private final Queue<Chunk> chunkQueue = new LinkedList<>();
    private Chunk currentChunk = null;
    private int x = 0;
    private int y = 0;
    private int z = 0;

    public GlowStoneFixer(JavaPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        run();
    }

    @Override
    public void run() {
        if (Bukkit.getWorld(world.getName()) == null) {
            plugin.getLogger().info(world.getName() + " is null!");
            return;
        }

        if (currentChunk != null && currentChunk.isLoaded()) {
            for (; y < 256; y++) {
                for (; x < 16; x++) {
                    for (; z < 16; z++) {
                        Block block = currentChunk.getBlock(x, y, z);
                        Material mat = block.getType();
                        if (mat == Material.GLOWSTONE || mat == Material.FIRE) {
                            block.setType(Material.GLASS);
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> block.setType(mat), 1);
                            plugin.getServer().getScheduler().runTaskLater(plugin, this, 1);
                            return;
                        }
                    }
                    z = 0;
                }
                x = 0;
            }
            y = 0;
        }

        if (chunkQueue.isEmpty()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                if (!doneChunks.contains(chunk.getX() + "," + chunk.getZ())) {
                    chunkQueue.add(chunk);
                    doneChunks.add(chunk.getX() + "," + chunk.getZ());
                }
            }

            if (chunkQueue.isEmpty()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, this, 200);
                return;
            }
        }

        currentChunk = chunkQueue.poll();
        plugin.getServer().getScheduler().runTaskLater(plugin, this, 1);
    }
}
