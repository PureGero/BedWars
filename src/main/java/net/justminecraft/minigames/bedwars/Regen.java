package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Regen implements Runnable {

    private final BedWars bedWars;
    private final HashMap<Player, Integer> lastRegen = new HashMap<>();
    private int ticks = 0;

    public Regen(BedWars bedWars) {
        this.bedWars = bedWars;

        bedWars.getServer().getScheduler().runTaskTimer(bedWars, this, 1, 1);
    }

    @Override
    public void run() {
        ticks++;
        for (Game game : MG.core().getGames(bedWars)) {
            for (Player player : game.players) {
                if (player.getHealth() < 20) {
                    int lastTick = lastRegen.getOrDefault(player, 0);
                    int delta = /*player.hasPotionEffect(PotionEffectType.REGENERATION) ? 25 : */100; // Regen pots are handled by minecraft
                    if (lastTick <= ticks - delta) {
                        player.setHealth(player.getHealth() + 1);
                        lastRegen.put(player, ticks);
                    }
                }
            }
        }
    }
}
