package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class TopWinStreaksDisplay extends Display<String, Integer> implements Listener {
    public TopWinStreaksDisplay(BedWars bedWars) {
        super(new Location(Bukkit.getWorlds().get(0), 136.5, 164.5, 368.5), 10, "Top Win Streaks", " win streak");
        bedWars.getServer().getPluginManager().registerEvents(this, bedWars);
        refresh();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        TopWinStreaks.updateWinStreak(PlayerData.get(e.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        TopWinStreaks.updateWinStreak(PlayerData.get(e.getPlayer().getUniqueId()));
        refresh();
    }

    @Override
    public DisplayEntry<String, Integer> getEntry(int i) {
        TopWinStreaks winStreak = i < TopWinStreaks.toplist.size() ? TopWinStreaks.toplist.get(i) : null;
        return new DisplayEntry<>(winStreak != null ? winStreak.getName() : "???", winStreak != null ? winStreak.getWinStreak() : 0);
    }
}
