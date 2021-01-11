package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.titleapi.TitleAPI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlayerRespawn implements Runnable {
    private final BedWars plugin;
    private final Player player;
    private final Block bed;
    private int seconds = 5;

    public PlayerRespawn(BedWars plugin, Player player, Block bed) {
        this.plugin = plugin;
        this.player = player;
        this.bed = bed;
        run();
    }

    @Override
    public void run() {
        if (seconds == 0) {
            Location spawnLocation = bed.getLocation().add(Math.random() * 6 - 3, 0, Math.random() * 6 - 3);
            while (spawnLocation.getBlock().getType() != Material.AIR) {
                spawnLocation = spawnLocation.add(0, 1, 0);
            }

            spawnLocation.setYaw(plugin.getAngleDegrees(spawnLocation));

            player.teleport(spawnLocation);
            player.setGameMode(GameMode.SURVIVAL);
            return;
        }

        TitleAPI.sendTitle(player, 0, seconds > 1 ? 30 : 20, 0, ChatColor.GREEN + "Respawning in " + seconds + " second" + (seconds == 1 ? "" : "s") + "...", "");
        seconds--;
        plugin.getServer().getScheduler().runTaskLater(plugin, this, 20);
    }
}
