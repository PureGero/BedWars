package net.justminecraft.minigames.bedwars;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class PlayerGrouper {
    private final List<Player> players;
    private final int playersPerTeam;

    private final Player[] currentPlayers;
    private final List<Player> closestPlayers;
    private double closestDelta = Double.MAX_VALUE;

    public PlayerGrouper(List<Player> players, int playersPerTeam, Consumer<Player> consumer) {
        this.players = players;
        this.playersPerTeam = playersPerTeam;
        this.closestPlayers = new ArrayList<>(players.subList(0, playersPerTeam));
        this.currentPlayers = new Player[playersPerTeam];

        long t = System.currentTimeMillis();
        search(0, 0);
        System.out.println("Grouping " + players.size() + " into a group of " + playersPerTeam + " players took " + (System.currentTimeMillis() - t) + "ms");

        closestPlayers.forEach(consumer);
    }

    private void search(int i, int r) {
        if (r == playersPerTeam) {
            double delta = calculateDelta();

            if (delta < closestDelta) {
                closestPlayers.clear();
                closestPlayers.addAll(Arrays.asList(currentPlayers));
                closestDelta = delta;
            }

            return;
        }

        for (; i < players.size(); i++) {
            currentPlayers[r] = players.get(i);
            search(i + 1, r + 1);
        }
    }

    private double calculateDelta() {
        double delta = 0;

        for (int i = 0; i < currentPlayers.length; i++) {
            Location from = currentPlayers[i].getLocation();
            for (int j = i + 1; j < currentPlayers.length; j++) {
                Location to = currentPlayers[j].getLocation();

                if (!from.getWorld().equals(to.getWorld())) {
                    delta += 1000 * 1000;
                } else {
                    delta += from.distanceSquared(to);
                }
            }
        }

        return delta;
    }
}
