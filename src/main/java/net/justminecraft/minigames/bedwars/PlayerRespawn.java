package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.titleapi.TitleAPI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlayerRespawn implements Runnable {
    private final BedWarsGame game;
    private final Player player;
    private final Block bed;
    private int seconds = 5;

    public PlayerRespawn(BedWarsGame game, Player player, Block bed) {
        this.game = game;
        this.player = player;
        this.bed = bed;
        run();
    }

    @Override
    public void run() {
        if (!game.players.contains(player)) {
            return;
        }
        
        if (seconds == 0) {
            Location spawnLocation = bed.getLocation().add(Math.random() * 6 - 3, 0, Math.random() * 6 - 3);
            while (spawnLocation.getBlock().getType() != Material.AIR) {
                spawnLocation = spawnLocation.add(0, 1, 0);
            }

            spawnLocation.setYaw(((BedWars) game.minigame).getAngleDegrees(spawnLocation));

            player.teleport(spawnLocation);
            player.setGameMode(GameMode.SURVIVAL);
            return;
        }

        TitleAPI.sendTitle(player, 0, seconds > 1 ? 30 : 20, 0, ChatColor.GREEN + "Respawning in " + seconds + " second" + (seconds == 1 ? "" : "s") + "...", "");
        seconds--;
        game.minigame.getServer().getScheduler().runTaskLater(game.minigame, this, 20);
    }
}
