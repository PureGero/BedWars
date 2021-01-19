package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.Minigame;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

public class BedWarsGame extends Game {

    private static final List<ChatColor> COLORS = Arrays.asList(
            ChatColor.AQUA,
            ChatColor.RED,
            ChatColor.GREEN,
            ChatColor.YELLOW,
            ChatColor.LIGHT_PURPLE,
            ChatColor.GOLD,
            ChatColor.DARK_PURPLE,
            ChatColor.DARK_GREEN
    );

    private static final List<Integer> COLOR_DATA = Arrays.asList(
            3,
            14,
            5,
            4,
            2,
            1,
            10,
            13
    );

    Scoreboard scoreboard;
    HashMap<Block, ColouredBed> beds = new HashMap<>();
    HashMap<Team, Location> teamSpawnLocations = new HashMap<>();
    HashMap<Team, Block> teamBeds = new HashMap<>();
    HashMap<Team, ChatColor> teamColors = new HashMap<>();
    HashMap<Team, Short> teamColorDatas = new HashMap<>();
    HashSet<Block> playerBlocks = new HashSet<>();
    HashMap<Player, Short> playerColours = new HashMap<>();
    HashMap<Team, HashMap<Enchantment, Integer>> enchantments = new HashMap<>();
    HashMap<Entity, Location> villagerSpawnLocations = new HashMap<>();
    int teamSize = 1;
    int teamCount;
    private BedWars bedwars;
    private Map map = null;

    public BedWarsGame(Minigame mg) {
        super(mg, false);
        bedwars = (BedWars) mg;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public Map randomMap() {
        if (map != null) {
            throw new IllegalStateException("map has already been set!");
        }

        return map = Map.values()[(int) (Math.random() * Map.values().length)];
    }

    public String getTeamName(Team team) {
        if (teamSize == 1 && !team.getEntries().isEmpty()) {
            return team.getPrefix() + team.getEntries().iterator().next();
        }

        for (ChatColor color : ChatColor.values()) {
            if (color.toString().equals(team.getPrefix())) {
                return getColorName(color);
            }
        }
        return ChatColor.WHITE + "Unknown";
    }

    public String getColorName(ChatColor color) {
        return color + color.name().substring(0, 1).toUpperCase() + color.name().substring(1).toLowerCase().replaceAll("_", " ");
    }

    private List<Location> getEmeraldSpawnLocations() {
        ArrayList<Location> locations = new ArrayList<>();

        for (Vector vector : map.getEmeraldVectors()) {
            locations.add(new Location(world, 0.5, 64, 0.5).add(vector));
        }

        return locations;
    }

    public List<Location> getDiamondSpawnLocations() {
        List<Location> locations = new ArrayList<>();

        for (int i = 0; i < teamCount; i++) {
            double a = Math.PI*2 * (i * 2 + 1) / (teamCount * 2);
            int x = (int) (Math.sin(a) * (map.getDistance() - 5));
            int z = (int) (-Math.cos(a) * (map.getDistance() - 5));
            locations.add(new Location(world, x, 64, z));
        }

        return locations;
    }

    public List<Location> getSpawnLocations() {
        List<Location> locations = new ArrayList<>();

        for (int i = 0; i < teamCount; i++) {
            double a = Math.PI*2 * i / teamCount;
            int x = (int) (Math.sin(a) * map.getDistance());
            int z = (int) (-Math.cos(a) * map.getDistance());
            if (x != 0 && z != 0) {
                x = (int) (Math.sin(a) * (map.getDistance() - 3));
                z = (int) (-Math.cos(a) * (map.getDistance() - 3));
            }
            locations.add(new Location(world, x, 64, z));
        }

        return locations;
    }

    public List<Location> getIslandSpawnLocations() {
        List<Location> locations = new ArrayList<>();

        if (teamCount == 1 || teamCount == 2 || teamCount == 4) {
            for (int i = 0; i < 16; i++) {
                if (i % (16 / teamCount / 2) == 0) {
                    continue;
                }
                double a = Math.PI*2 * i / 16;
                int x = (int) (Math.sin(a) * (map.getDistance() - 5));
                int z = (int) (-Math.cos(a) * (map.getDistance() - 5));
                locations.add(new Location(world, x, 64, z));
            }
        } else if (teamCount == 3) {
            for (int i = 0; i < 12; i++) {
                if (i % (12 / teamCount / 2) == 0) {
                    continue;
                }
                double a = Math.PI*2 * i / 12;
                int x = (int) (Math.sin(a) * (map.getDistance() - 5));
                int z = (int) (-Math.cos(a) * (map.getDistance() - 5));
                locations.add(new Location(world, x, 64, z));
            }
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
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && (item.getType() == Material.IRON_INGOT || item.getType() == Material.DIAMOND || item.getType() == Material.EMERALD)) {
                p.getWorld().dropItemNaturally(p.getLocation(), item);
            }
        }

        p.getInventory().clear();
        p.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
        p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
        UpgradesShop.updateEnchants(p);

        Team team = scoreboard.getEntryTeam(p.getName());

        if (teamBeds.get(team).getType() != Material.BED_BLOCK) {
            playerLeave(p);

            updateScore(team);
        } else {
            p.setVelocity(new Vector(0, 0, 0));
            p.setHealth(20);
            p.setFallDistance(0);
            p.setGameMode(GameMode.SPECTATOR);
            new PlayerRespawn(this, p, teamBeds.get(team));

            if (p.getLocation().getY() < 30) {
                p.teleport(new Location(p.getWorld(), 20, 90, 20, 135, 45));
            }
        }
    }

    @Override
    public boolean isGameOver() {
        Team team = null;
        for (Player player : players) {
            Team team2 = scoreboard.getEntryTeam(player.getName());

            if (team2 == null) {
                continue;
            }

            if (team == null) {
                team = team2;
            } else if (!team.equals(team2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getWinningTeamName() {
        for (Player player : players) {
            Team team = scoreboard.getEntryTeam(player.getName());

            if (team != null) {
                return getTeamName(team);
            }
        }

        return "Unknown";
    }

    private boolean hasDroppedStartingIron = false;
    public void ironTicker() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(minigame, () -> {
            if (!players.isEmpty()) {
                teamSpawnLocations.forEach((team, location) -> dropItem(
                        location.clone().add(rotate(map.getIronVector(), bedwars.getAngle(location))).add(0.5, 0, 0.5),
                        Material.IRON_INGOT,
                        hasDroppedStartingIron ? enchantments.get(team).getOrDefault(Enchantment.LURE, 0) + 1 : 8
                ));
                hasDroppedStartingIron = true;
                ironTicker();
            }
        }, 20 * 4);
    }

    public void diamondTicker() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(minigame, () -> {
            if (!players.isEmpty()) {
                getDiamondSpawnLocations().forEach(location -> dropItem(location.add(0.5, 2, 0.5), Material.DIAMOND, 1));
                diamondTicker();
            }
        }, 20 * 15);
    }

    public void emeraldTicker() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(minigame, () -> {
            if (!players.isEmpty()) {
                getEmeraldSpawnLocations().forEach(location -> dropItem(location, Material.EMERALD, 1));
                emeraldTicker();
            }
        }, 20 * 30);
    }

    private void dropItem(Location location, Material mat, int count) {
        ItemStack item = new ItemStack(mat, count);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("RAW");
        item.setItemMeta(meta);
        location.getWorld().dropItem(location, item);
    }

    public void spawnVillagers() {
        getSpawnLocations().forEach(spawnLocation -> {
            Location location = spawnLocation.clone().add(0.5, 0, 0.5).add(rotate(map.getShopVector(), bedwars.getAngle(spawnLocation)));
            location.setYaw((float) (bedwars.getAngleDegrees(spawnLocation) - 90));
            Villager villager = location.getWorld().spawn(location, Villager.class);
            villager.setProfession(Villager.Profession.BLACKSMITH);
            villager.setCustomName(ChatColor.GOLD + "Items Shop");
            villager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60 * 60 * 20, 127, true, false));
            villagerSpawnLocations.put(villager, location);

            location = spawnLocation.clone().add(0.5, 0, 0.5).add(rotate(map.getUpgradeVector(), bedwars.getAngle(spawnLocation)));
            location.setYaw((float) (bedwars.getAngleDegrees(spawnLocation) + 90));
            villager = location.getWorld().spawn(location, Villager.class);
            villager.setProfession(Villager.Profession.LIBRARIAN);
            villager.setCustomName(ChatColor.GREEN + "Upgrades Shop");
            villager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60 * 60 * 20, 127, true, false));
            villagerSpawnLocations.put(villager, location);
        });

        Location location = new Location(world, 0, 64, 0).add(0.5, 0, 0.5).add(map.getMiscVector());
        Villager villager = location.getWorld().spawn(location, Villager.class);
        villager.setProfession(Villager.Profession.PRIEST);
        villager.setCustomName(ChatColor.AQUA + "Geoff Walter");
        villager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60 * 60 * 20, 127, true, false));
        villagerSpawnLocations.put(villager, location);
    }

    public void sendBeds(Player p) {
        beds.forEach((block, bed) -> {
            if (block.getType() == Material.BED_BLOCK) {
                bed.send(p);
            }
        });
    }

    private Vector rotate(Vector vector, double radians) {
        return new Vector(
                Math.cos(radians) * vector.getX() + -Math.sin(radians) * vector.getZ(),
                vector.getY(),
                -Math.sin(radians) * vector.getX() + Math.cos(radians) * vector.getZ()
        );
    }

    public void updateScore(Team team) {
        int teamMembers = 0;

        for (String member : team.getEntries()) {
            if (players.contains(Bukkit.getPlayerExact(member))) {
                teamMembers ++;
            }
        }

        scoreboard.resetScores(getTeamName(team) + ChatColor.WHITE + ": " + ChatColor.GREEN + "❤");
        scoreboard.resetScores(getTeamName(team) + ChatColor.WHITE + ": " + ChatColor.YELLOW + (teamMembers + 1));

        if (teamMembers == 0) {
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(getTeamName(team) + ChatColor.RED + ": " + ChatColor.RED + "✗").setScore(3);
        } else {
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(getTeamName(team) + ChatColor.WHITE + ": " + ChatColor.YELLOW + teamMembers).setScore(3);
        }
    }
}
