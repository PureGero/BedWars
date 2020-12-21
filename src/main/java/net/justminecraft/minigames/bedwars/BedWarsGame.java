package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BedWarsGame extends Game {

    public BedWarsGame(Minigame mg) {
        super(mg, false);
    }

    public List<Location> getDiamondSpawnLocations() {
        return Collections.singletonList(new Location(world, 0, 64, 0));
    }

    public List<Location> getSpawnLocations() {
        List<Location> locations = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            double a = Math.PI*2 * i / players.size();
            int x = (int) Math.cos(a) * 70;
            int z = (int) Math.sin(a) * 70;
            locations.add(new Location(world, x, 64, z));
        }

        return locations;
    }

    @Override
    public void onPlayerDeath(Player p) {
        if (p.getBedSpawnLocation().getBlock().getType() != Material.BED_BLOCK) {
            playerLeave(p);
        } else {
            p.spigot().respawn();
        }
    }

    public void ironTicker() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(minigame, () -> {
            if (!players.isEmpty()) {
                getSpawnLocations().forEach(location -> location.getWorld().dropItem(location.add(0.5, 2, 0.5), new ItemStack(Material.IRON_INGOT)));
                ironTicker();
            }
        }, 20);
    }

    public void diamondTicker() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(minigame, () -> {
            if (!players.isEmpty()) {
                getDiamondSpawnLocations().forEach(location -> location.getWorld().dropItem(location.add(0.5, 2, 0.5), new ItemStack(Material.DIAMOND)));
                diamondTicker();
            }
        }, 20 * 5);
    }

    public void spawnVillagers() {
        getSpawnLocations().forEach(location -> {
            Villager villager = location.getWorld().spawn(location.clone().add(0.5, 0, 2.5), Villager.class);
        });
    }
}
