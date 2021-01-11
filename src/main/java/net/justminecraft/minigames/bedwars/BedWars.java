package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.minigamecore.Minigame;
import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class BedWars extends Minigame implements Listener {

    public static File DATA_FOLDER;

    public void onEnable() {
        DATA_FOLDER = getDataFolder();

        MG.core().registerMinigame(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("BedWars enabled");
    }

    public void onDisable() {
        getLogger().info("BedWars disabled");
    }

    @Override
    public int getMaxPlayers() {
        return 9;
    }

    @Override
    public int getMinPlayers() {
        return 2;
    }

    @Override
    public String getMinigameName() {
        return "BedWars";
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Game g = MG.core().getGame(e.getEntity());
        if (g != null && g.minigame == this)
            g.broadcastRaw(e.getDeathMessage());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getFrom().getChunk() != e.getTo().getChunk()) {
            Game g = MG.core().getGame(e.getPlayer());
            if (g != null && g.minigame == this) {
                BedWarsGame game = (BedWarsGame) g;
                game.sendBeds(e.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Game g = MG.core().getGame(e.getPlayer());
        if (g instanceof BedWarsGame) {
            BedWarsGame game = (BedWarsGame) g;
            if (e.getBlock().getType() == Material.TNT) {
                e.getBlock().setType(Material.AIR);
                TNTPrimed tnt = e.getBlock().getWorld().spawn(e.getBlock().getLocation().add(0.5, 0.65, 0.5), TNTPrimed.class);
                setSource(tnt, e.getPlayer());
                tnt.setFuseTicks(40);
            } else {
                game.playerBlocks.add(e.getBlock());
            }
        }
    }

    private void setSource(TNTPrimed tnt, LivingEntity source) {
        try {
            Method tntGetHandle = tnt.getClass().getDeclaredMethod("getHandle");
            Method entityGetHandle = source.getClass().getDeclaredMethod("getHandle");

            Object craftTnt = tntGetHandle.invoke(tnt);
            Object craftEntity = entityGetHandle.invoke(source);

            Field sourceField = craftTnt.getClass().getDeclaredField("source");
            sourceField.setAccessible(true);

            sourceField.set(craftTnt, craftEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Game g = MG.core().getGame(e.getPlayer());
        if (g instanceof BedWarsGame) {
            BedWarsGame game = (BedWarsGame) g;
            if (e.getBlock().getType() == Material.BED_BLOCK) {
                onBedBreak(game, e.getPlayer(), e.getBlock());
                e.setCancelled(true);
            } else if (!game.playerBlocks.contains(e.getBlock())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You can only break player placed blocks!");
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof Shop) {
            ((Shop) e.getInventory().getHolder()).onClick(e);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent e) {
        Game g = MG.core().getGame(e.getPlayer());
        if (g instanceof BedWarsGame) {
            BedWarsGame game = (BedWarsGame) g;
            if (e.getRightClicked() instanceof Villager) {
                if (((Villager) e.getRightClicked()).getProfession() == Villager.Profession.LIBRARIAN) {
                    e.getPlayer().openInventory(new UpgradesShop(e.getPlayer()).getInventory());
                } else {
                    e.getPlayer().openInventory(new ItemsShop(game.playerColours.get(e.getPlayer())).getInventory());
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Game g = MG.core().getGame(e.getPlayer());
        if (g instanceof BedWarsGame) {
            if (e.getItemDrop().getItemStack().getType() != Material.IRON_INGOT
                    && e.getItemDrop().getItemStack().getType() != Material.DIAMOND
                    && e.getItemDrop().getItemStack().getType() != Material.EMERALD) {
                e.setCancelled(true);
            }
        }
    }

    private void onBedBreak(BedWarsGame game, Player player, Block block) {
        Team bedTeam = null;

        for (HashMap.Entry<Team, Location> entry : game.teamSpawnLocations.entrySet()) {
            if (entry.getValue().distanceSquared(block.getLocation()) < 16) {
                bedTeam = entry.getKey();
            }
        }

        if (bedTeam != null) {
            for (String member : bedTeam.getEntries()) {
                Player p = Bukkit.getPlayerExact(member);
                if (game.players.contains(p)) {
                    p.sendMessage(ChatColor.BOLD + "Your bed has been destroyed!!");
                }
            }

            Team team = game.scoreboard.getEntryTeam(player.getName());
            game.broadcast(team.getPrefix() + player.getName() + ChatColor.GOLD + " has broken " + game.getTeamName(bedTeam) + "'s bed" + ChatColor.GOLD + "!");

            game.updateScore(bedTeam);
        }

        setBedToAir(block);
    }

    private void setBedToAir(Block block) {
        block.setType(Material.AIR, false);
        for (BlockFace face : new BlockFace[] {
                BlockFace.SOUTH,
                BlockFace.NORTH,
                BlockFace.WEST,
                BlockFace.EAST
        }) {
            if (block.getRelative(face).getType() == Material.BED_BLOCK) {
                setBedToAir(block.getRelative(face));
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            Game g = MG.core().getGame(e.getPlayer());
            if (g != null && g.minigame == this) {
                BedWarsGame game = (BedWarsGame) g;
                ColouredBed bed = game.beds.get(e.getClickedBlock());
                if (bed != null) {
                    Bukkit.getScheduler().runTaskLater(this, () -> bed.send(e.getPlayer()), 0);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Game g = MG.core().getGame((Player) e.getEntity());
            if (g != null && g.minigame == this) {
                if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    e.setCancelled(false);
                    e.setDamage(1000);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Villager && e.getEntity().getLocation().getY() > 0 && MG.core().getGame(e.getEntity().getWorld()) instanceof BedWarsGame) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (MG.core().getGame(e.getEntity().getWorld()) instanceof BedWarsGame) {
            BedWarsGame game = (BedWarsGame) MG.core().getGame(e.getEntity().getWorld());
            e.blockList().removeIf(block -> !game.playerBlocks.contains(block));
        }
    }

    @Override
    public Game newGame() {
        return new BedWarsGame(this);
    }

    @Override
    public void startGame(Game game) {
        BedWarsGame g = (BedWarsGame) game;

        g.world.setDifficulty(Difficulty.PEACEFUL);
        g.world.setSpawnLocation(0, 64, 0);

        for (int x = -5; x < 5; x++) {
            for (int z = -5; z < 5; z++) {
                g.world.getChunkAt(x, z).load();
            }
        }

        List<Location> spawnLocations = g.getSpawnLocations();
        List<ChatColor> colors = g.getColors();
        List<Integer> colorDatas = g.getColorData();

        Objective bedAlive = g.scoreboard.registerNewObjective("bedAlive", "dummy");
        bedAlive.setDisplayName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "BED WARS");
        bedAlive.setDisplaySlot(DisplaySlot.SIDEBAR);

        bedAlive.getScore("  ").setScore(4);
        bedAlive.getScore(" ").setScore(2);
        bedAlive.getScore(ChatColor.YELLOW + "justminecraft.net").setScore(1);

        for (Player player : g.players) {
            MG.resetPlayer(player);
            Location spawnLocation = spawnLocations.remove(0);
            ChatColor color = colors.remove(0);
            int colorData = colorDatas.remove(0);

            spawnLocation.setYaw(getAngleDegrees(spawnLocation));

            player.teleport(spawnLocation.clone().add(0.5, 0, 0.5));
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 1);
            g.minigame.message(player, "Game has started!");
            player.sendMessage("Destroy the other player's beds to stop them from respawning!");
            player.sendMessage("Buy gear from the villagers with iron and diamonds!");

            player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
            player.getEquipment().setChestplate(dye(Material.LEATHER_CHESTPLATE, color));
            player.getEquipment().setLeggings(dye(Material.LEATHER_LEGGINGS, color));

            Team team = g.scoreboard.getTeam(g.getColorName(color));
            if (team == null) {
                team = g.scoreboard.registerNewTeam(g.getColorName(color));
                team.setPrefix(color.toString());
                team.setAllowFriendlyFire(false);
                team.setCanSeeFriendlyInvisibles(true);
                g.teamSpawnLocations.put(team, spawnLocation);
                g.teamBeds.put(team, spawnLocation.clone().add(-Math.sin(getAngle(spawnLocation)) * 2 + 0.5, 0, Math.cos(getAngle(spawnLocation)) * 2 + 0.5).getBlock());
            }

            player.setScoreboard(g.scoreboard);
            team.addEntry(player.getName());

            for (Block bed : new Block[] {
                    spawnLocation.getBlock().getRelative((int) -Math.sin(getAngle(spawnLocation)) * 2, 0, (int) Math.cos(getAngle(spawnLocation)) * 2),
                    spawnLocation.getBlock().getRelative((int) -Math.sin(getAngle(spawnLocation)) * 3, 0, (int) Math.cos(getAngle(spawnLocation)) * 3),
            }) {
                g.beds.put(bed, new ColouredBed(bed, colorData));
            }

            g.playerColours.put(player, (short) colorData);
        }

        for (Team team : g.scoreboard.getTeams()) {
            g.enchantments.put(team, new HashMap<>());
            bedAlive.getScore(g.getTeamName(team) + ChatColor.WHITE + ": " + ChatColor.GREEN + "❤").setScore(3);
        }

        Bukkit.getScheduler().runTaskLater(this, () -> g.players.forEach(g::sendBeds), 20);

        // Fix players not seeing eachother bug
        Bukkit.getScheduler().scheduleSyncDelayedTask(BedWars.this, () -> {
            for (Player p : g.players) {
                p.teleport(p.getLocation());
            }
        }, 1);

        g.ironTicker();
        g.diamondTicker();
        g.emeraldTicker();
        g.spawnVillagers();
    }

    private ItemStack dye(Material material, ChatColor chatColor) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(translateChatColorToColor(chatColor));
        item.setItemMeta(meta);
        return item;
    }

    private static Color translateChatColorToColor(ChatColor chatColor) {
        switch (chatColor) {
            case AQUA:
                return Color.AQUA;
            case BLACK:
                return Color.BLACK;
            case BLUE:
                return Color.BLUE;
            case DARK_AQUA:
                return Color.BLUE;
            case DARK_BLUE:
                return Color.BLUE;
            case DARK_GRAY:
                return Color.GRAY;
            case DARK_GREEN:
                return Color.GREEN;
            case DARK_PURPLE:
                return Color.PURPLE;
            case DARK_RED:
                return Color.RED;
            case GOLD:
                return Color.YELLOW;
            case GRAY:
                return Color.GRAY;
            case GREEN:
                return Color.GREEN;
            case LIGHT_PURPLE:
                return Color.PURPLE;
            case RED:
                return Color.RED;
            case WHITE:
                return Color.WHITE;
            case YELLOW:
                return Color.YELLOW;
            default:
                return null;
        }
    }

    @Override
    public void generateWorld(Game game, WorldBuffer w) {
        BedWarsGame g = (BedWarsGame) game;

        g.moneyPerDeath = 5;
        g.moneyPerWin = 30;
        g.disableBlockBreaking = false;
        g.disableBlockPlacing = false;
        g.disableHunger = true;
        g.disablePvP = false;

        Map m = g.randomMap();

        long t = System.currentTimeMillis();
        for (int x = -10; x < 10; x++) {
            for (int z = -10; z < 10; z++) {
                w.blankChunk(x, z);
            }
        }

        List<Integer> colorData = g.getColorData();

        generateEmeraldIsland(m, w, new Location(g.world, 0, 64, 0));
        g.getDiamondSpawnLocations().forEach(location -> generateDiamondIsland(m, w, location));
        g.getSpawnLocations().forEach(location -> generatePlayerIsland(m, w, location, colorData.remove(0)));
        g.getIslandSpawnLocations().forEach(location -> generateIsland(m, w, location));

        getLogger().info("Generated map in " + (System.currentTimeMillis() - t) + "ms");
    }

    private void generateEmeraldIsland(Map m, WorldBuffer w, Location l) {
        m.placeSchematic(w, l, "emerald_small");
    }

    private void generateDiamondIsland(Map m, WorldBuffer w, Location l) {
        int angleDeg;
        double angle = Math.toDegrees(Math.atan2(l.getBlockX(), -l.getBlockZ()));
        if (angle <= -90) {
            angleDeg = 270;
        } else if (angle <= 0) {
            angleDeg = 180;
        } else if (angle <= 90) {
            angleDeg = 0;
        } else {
            angleDeg = 90;
        }
        m.placeSchematic(w, l, "diamond", angleDeg);
    }

    private void generatePlayerIsland(Map m, WorldBuffer w, Location l, int color) {
        m.placeSchematic(w, l, "player", getAngleDegrees(l), color);
    }

    private void generateIsland(Map m, WorldBuffer w, Location l) {
        m.placeSchematic(w, l, "island", getAngleDegrees(l));
    }

    public int getAngleDegrees(Location location) {
        double angle = Math.toDegrees(Math.atan2(location.getBlockX(), -location.getBlockZ()));
        if (angle < -135) {
            return 180;
        } else if (angle <= -45) {
            return 270;
        } else if (angle < 45) {
            return 0;
        } else if (angle <= 135) {
            return 90;
        } else {
            return 180;
        }
    }

    public double getAngle(Location location) {
        return Math.toRadians(getAngleDegrees(location));
    }


}
