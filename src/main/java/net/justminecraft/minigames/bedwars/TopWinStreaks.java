package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.Map;

public class TopWinStreaks {

    private static final String TOPWINSTREAKS_CSV = "topwinstreaks.csv";
    public static final List<TopWinStreaks> toplist = new ArrayList<>();
    private static final Map<UUID, TopWinStreaks> toplistIndex = new HashMap<>();

    private final UUID uuid;
    private String name;
    private int winStreak;

    private TopWinStreaks(UUID uuid, String name, int winStreak) {
        this.uuid = uuid;
        this.name = name;
        this.winStreak = winStreak;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getWinStreak() {
        return winStreak;
    }

    public static void load(File folder) {
        File file = new File(folder, TOPWINSTREAKS_CSV);
        if (file.isFile()) {
            try {
                toplist.clear();
                toplistIndex.clear();
                for (String line : Files.readAllLines(file.toPath())) {
                    try {
                        String[] parts = line.split(",");
                        toplist.add(toplistIndex.computeIfAbsent(UUID.fromString(parts[0]), uuid -> new TopWinStreaks(uuid, parts[1], Integer.parseInt(parts[2]))));
                    } catch (Exception ignored) {}
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void save(File folder) {
        sort();

        List<String> lines = new ArrayList<>();

        for (TopWinStreaks winStreak : toplist) {
            lines.add(winStreak.uuid + "," + winStreak.name + "," + winStreak.winStreak);
        }

        folder.mkdirs();

        try {
            Files.write(new File(folder, TOPWINSTREAKS_CSV).toPath(), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sort() {
        // Sort is basically O(n) since the list is already mostly sorted
        for (int i = 1; i < toplist.size(); i++) {
            sort(i);
        }
    }

    private static void sort(int i) {
        if (i <= 0) {
            return;
        }

        if (toplist.get(i - 1).winStreak < toplist.get(i).winStreak) {
            swap(i - 1, i);
            sort(i - 1);
        }
    }

    private static void swap(int i, int j) {
        TopWinStreaks temp = toplist.get(i);
        toplist.set(i, toplist.get(j));
        toplist.set(j, temp);
    }

    public static void updateWinStreak(PlayerData pd) {
        int stat = pd.getStat("BedWars", "winStreak");
        if (stat > 0 || toplistIndex.containsKey(pd.uuid)) {
            TopWinStreaks topWinStreak = toplistIndex.computeIfAbsent(pd.uuid, uuid -> {
                TopWinStreaks winStreak = new TopWinStreaks(uuid, Bukkit.getOfflinePlayer(pd.uuid).getName(), 0);
                toplist.add(winStreak);
                return winStreak;
            });
            if (Bukkit.getPlayer(pd.uuid) != null) {
                topWinStreak.name = Bukkit.getPlayer(pd.uuid).getName();
            }
            if (topWinStreak.winStreak != stat) {
                topWinStreak.winStreak = stat;
                sort();
            }
        }
    }

    public static boolean command(CommandSender sender, String label, String[] args) {
        int page = 1;
        try {
            if (args.length > 0)
                page = Integer.parseInt(args[0]);
            if (page < 1 || page - 1 > toplist.size() / 10)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid page.");
            return false;
        }
        sender.sendMessage(ChatColor.YELLOW + " --- Top win streaks (Page " + page + ") --- ");
        sender.sendMessage(ChatColor.GOLD + "Next page: /" + label + " " + (page + 1));
        for (int i = page * 10 - 10; i < page * 10 && i < toplist.size(); i++) {
            sender.sendMessage(ChatColor.YELLOW + (sender.getName().equals(toplist.get(i).name) ? ChatColor.BOLD.toString() : "") + " " + (i + 1) + ". " + toplist.get(i).name + " (" + prettyWinStreak(toplist.get(i).winStreak) + " win streak)");
        }
        return true;
    }

    private static String prettyWinStreak(int winStreak) {
        if (winStreak > 10000000) {
            return winStreak / 1000000 + "M";
        } else if (winStreak > 1000000) {
            return (winStreak / 100000) / 10.0 + "M";
        } else if (winStreak > 10000) {
            return winStreak / 1000 + "k";
        } else if (winStreak > 1000) {
            return (winStreak / 100) / 10.0 + "k";
        } else {
            return winStreak + "";
        }
    }
}
