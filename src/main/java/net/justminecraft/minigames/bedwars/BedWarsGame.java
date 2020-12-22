package net.justminecraft.minigames.bedwars;

import com.gmail.val59000mc.villagerapi.VillagerTrade;
import com.gmail.val59000mc.villagerapi.VillagerTradeApi;
import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class BedWarsGame extends Game {

    private static final List<ChatColor> COLORS = Arrays.asList(
            ChatColor.AQUA,
            ChatColor.RED,
            ChatColor.GREEN,
            ChatColor.YELLOW,
            ChatColor.GOLD,
            ChatColor.LIGHT_PURPLE,
            ChatColor.DARK_PURPLE,
            ChatColor.DARK_GREEN
    );

    private static final List<Integer> COLOR_DATA = Arrays.asList(
            3,
            14,
            5,
            4,
            1,
            2,
            10,
            13
    );

    Scoreboard scoreboard;
    HashMap<Block, ColouredBed> beds = new HashMap<>();

    public BedWarsGame(Minigame mg) {
        super(mg, false);
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
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

    public List<ChatColor> getColors() {
        return new ArrayList<>(COLORS);
    }

    public List<Integer> getColorData() {
        return new ArrayList<>(COLOR_DATA);
    }

    @Override
    public void onPlayerDeath(Player p) {
        if (p.getBedSpawnLocation() == null || p.getBedSpawnLocation().getBlock().getType() != Material.BED_BLOCK) {
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
        }, 20 * 2);
    }

    public void diamondTicker() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(minigame, () -> {
            if (!players.isEmpty()) {
                getDiamondSpawnLocations().forEach(location -> location.getWorld().dropItem(location.add(0.5, 2, 0.5), new ItemStack(Material.DIAMOND)));
                diamondTicker();
            }
        }, 20 * 10);
    }

    public void spawnVillagers() {
        List<Integer> colors = getColorData();
        getSpawnLocations().forEach(location -> {
            int color = colors.remove(0);

            Villager villager = location.getWorld().spawn(location.clone().add(0.5, 0, 2.5), Villager.class);
            VillagerTradeApi.clearTrades(villager);
            VillagerTradeApi.addTrade(villager, new VillagerTrade(new ItemStack(Material.IRON_INGOT), null, new ItemStack(Material.WOOL, 16, (short) color)));
            VillagerTradeApi.addTrade(villager, new VillagerTrade(new ItemStack(Material.DIAMOND), null, new ItemStack(Material.STAINED_CLAY, 8, (short) color)));
            VillagerTradeApi.addTrade(villager, new VillagerTrade(new ItemStack(Material.IRON_INGOT, 10), null, new ItemStack(Material.SHEARS)));
            VillagerTradeApi.addTrade(villager, new VillagerTrade(new ItemStack(Material.IRON_INGOT, 10), null, new ItemStack(Material.IRON_PICKAXE)));
            VillagerTradeApi.addTrade(villager, new VillagerTrade(new ItemStack(Material.DIAMOND, 10), null, new ItemStack(Material.DIAMOND_PICKAXE)));
            VillagerTradeApi.addTrade(villager, new VillagerTrade(new ItemStack(Material.DIAMOND, 2), null, new ItemStack(Material.ENDER_PEARL)));

            villager = location.getWorld().spawn(location.clone().add(0.5, 0, -2.5), Villager.class);
            VillagerTradeApi.clearTrades(villager);
            VillagerTradeApi.addTrade(villager, new VillagerTrade(new ItemStack(Material.IRON_INGOT, 6), null, new ItemStack(Material.IRON_SWORD)));
            VillagerTradeApi.addTrade(villager, new VillagerTrade(new ItemStack(Material.DIAMOND, 4), null, new ItemStack(Material.DIAMOND_SWORD)));
            VillagerTradeApi.addTrade(villager, new VillagerTrade(new ItemStack(Material.IRON_INGOT, 6), null, new ItemStack(Material.IRON_CHESTPLATE)));
            VillagerTradeApi.addTrade(villager, new VillagerTrade(new ItemStack(Material.DIAMOND, 4), null, new ItemStack(Material.DIAMOND_CHESTPLATE)));
        });
    }

    public void sendBeds(Player p) {
        beds.forEach((block, bed) -> {
            if (block.getType() == Material.BED_BLOCK) {
                bed.send(p);
            }
        });
    }
}
