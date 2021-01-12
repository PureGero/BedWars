package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import org.bukkit.entity.Player;

public class Regen implements Runnable {

    private final BedWars bedWars;

    public Regen(BedWars bedWars) {
        this.bedWars = bedWars;

        bedWars.getServer().getScheduler().runTaskTimer(bedWars, this, 100, 100);
    }

    @Override
    public void run() {
        for (Game game : MG.core().getGames(bedWars)) {
            for (Player player : game.players) {
                if (player.getHealth() < 20) {
                    player.setHealth(player.getHealth() + 1);
                }
            }
        }
    }
}
