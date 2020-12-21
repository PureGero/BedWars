package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.minigamecore.Minigame;
import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class BedWars extends Minigame implements Listener {

    public void onEnable() {
        MG.core().registerMinigame(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("BedWars enabled");
    }

    public void onDisable() {
        getLogger().info("BedWars disabled");
    }

    @Override
    public int getMaxPlayers() {
        return 9;
    }

    @Override
    public int getMinPlayers() {
        return 2;
    }

    @Override
    public String getMinigameName() {
        return "BedWars";
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Game g = MG.core().getGame(e.getEntity());
        if (g != null && g.minigame == this)
            g.broadcastRaw(e.getDeathMessage());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.STONE) {
            Game g = MG.core().getGame(e.getPlayer());
            if (g != null && g.minigame == this) {
                BedWarsGame s = (BedWarsGame) g;

            }
        }
    }

    @Override
    public Game newGame() {
        return new BedWarsGame(this);
    }

    @Override
    public void startGame(Game game) {
        BedWarsGame g = (BedWarsGame) game;

        g.world.setDifficulty(Difficulty.PEACEFUL);
        g.world.setSpawnLocation(0, 64, 0);

        for (int x = -5; x < 5; x++) {
            for (int z = -5; z < 5; z++) {
                g.world.getChunkAt(x, z).load();
            }
        }

        List<Location> spawnLocations = g.getSpawnLocations();

        for (Player p : g.players) {
            Location spawnLocation = spawnLocations.remove(0);
            p.teleport(spawnLocation.add(0.5, 0, 0.5));
            MG.resetPlayer(p);
            p.setBedSpawnLocation(spawnLocation.clone().add(2, 0, 0));
            p.playSound(p.getLocation(), Sound.LEVEL_UP, 2, 1);
            g.minigame.message(p, "Game has started!");
            p.sendMessage("Destroy the other player's beds to stop them from respawning!");
            p.sendMessage("Buy gear from the villagers with iron and diamonds!");
        }

        // Fix players not seeing eachother bug
        Bukkit.getScheduler().scheduleSyncDelayedTask(BedWars.this, () -> {
            for (Player p : g.players) {
                p.teleport(p.getLocation());
            }
        }, 1);

        g.ironTicker();
        g.diamondTicker();
        g.spawnVillagers();
    }

    @Override
    public void generateWorld(Game game, WorldBuffer w) {
        BedWarsGame g = (BedWarsGame) game;

        g.moneyPerDeath = 5;
        g.moneyPerWin = 30;
        g.disableBlockBreaking = false;
        g.disableBlockPlacing = false;
        g.disableHunger = true;

        long t = System.currentTimeMillis();
        for (int x = -10; x < 10; x++)
            for (int z = -10; z < 10; z++)
                w.blankChunk(x, z);

        g.getDiamondSpawnLocations().forEach(location -> generateDiamondIsland(w, location));
        g.getSpawnLocations().forEach(location -> generatePlayerIsland(w, location));

        getLogger().info("Generated map in " + (System.currentTimeMillis() - t) + "ms");
    }

    private void generateDiamondIsland(WorldBuffer w, Location l) {
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                w.setBlockAt(l.clone().add(x, -1, z), Material.STONE);
            }
        }
        w.setBlockAt(l.clone().add(0, -1, 0), Material.DIAMOND_BLOCK);
    }

    private void generatePlayerIsland(WorldBuffer w, Location l) {
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                w.setBlockAt(l.clone().add(x, -1, z), Material.GRASS);
            }
        }
        w.setBlockAt(l.clone().add(0, -1, 0), Material.IRON_BLOCK);
        w.setBlockAt(l.clone().add(1, 0, 0), Material.BED_BLOCK);
        w.setBlockAt(l.clone().add(2, 0, 0), Material.BED_BLOCK);
    }


}
