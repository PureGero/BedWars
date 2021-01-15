package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.*;
import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import net.justminecraft.minigames.titleapi.TitleAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BedWars extends Minigame implements Listener {

    public static File DATA_FOLDER;

    private static HashMap<Player, TeamPreference> teamsPreference = new HashMap<>();
    private static HashMap<TeamPreference, Location> preferenceArmorStandLocations = new HashMap<>();

    public void onEnable() {
        DATA_FOLDER = getDataFolder();

        MG.core().registerMinigame(this);
        getServer().getPluginManager().registerEvents(this, this);
        new Regen(this);
        getLogger().info("BedWars enabled");

        spawnTeamPreferenceVillages();
    }

//    private void noAI(EntityInsentient entity) {
//        try {
//            Field field = entity.goalSelector.getClass().getDeclaredField("b");
//            field.setAccessible(true);
//
//            ((List<?>) field.get(entity.goalSelector)).clear();
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void spawnTeamPreferenceVillages() {
//        Function<Villager, Runnable> setNoAiLater = v -> () -> ((CraftVillager) v).getHandle().k(true); // NoAI
//        BiFunction<Entity, Location, Runnable> teleportLater = (e, l) -> () -> {
//            ((CraftVillager) e).getHandle().setPositionRotation(138.5, 167, 389.5, 170, 0);
//        };
//
//        Location location = new Location(Bukkit.getWorlds().get(0), 138.5, 167, 389.5, 170, 0);
//        location.getWorld().getNearbyEntities(location, 1, 1, 1).forEach(entity -> {
//            if (entity instanceof Villager) {
//                entity.remove();
//            }
//        });
//
//        Villager villager = location.getWorld().spawn(location, Villager.class);
//        villager.setProfession(Villager.Profession.BLACKSMITH);
//        villager.setCustomName(ChatColor.GREEN + "Solo");
//        villager.setCustomNameVisible(true);
//        noAI(((CraftVillager) villager).getHandle());
//        ((CraftVillager) villager).getHandle().setPositionRotation(138.5, 167, 389.5, 170, 0);
//        Bukkit.getScheduler().runTaskTimer(this, teleportLater.apply(villager, location), 20, 20);
//
//        location = new Location(Bukkit.getWorlds().get(0), 134.5, 167, 389.5, -170, 0);
//        location.getWorld().getNearbyEntities(location, 1, 1, 1).forEach(entity -> {
//            if (entity instanceof Villager) {
//                entity.remove();
//            }
//        });
//
//        villager = location.getWorld().spawn(location, Villager.class);
//        villager.setProfession(Villager.Profession.LIBRARIAN);
//        villager.setCustomName(ChatColor.AQUA + "Teams");
//        villager.setCustomNameVisible(true);
//        Bukkit.getScheduler().runTaskLater(this, setNoAiLater.apply(villager), 10);
    }

    public void onDisable() {
        getLogger().info("BedWars disabled");
    }

    @Override
    public int getMaxPlayers() {
        return 32;
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
                } else if (((Villager) e.getRightClicked()).getProfession() == Villager.Profession.PRIEST) {
                    e.getPlayer().openInventory(new MiscShop().getInventory());
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
                    TitleAPI.sendTitle(p, 10, 100, 10, bedTeam.getPrefix() + player.getName() + " has broken your bed!", ChatColor.GOLD + "You will no longer be able to respawn");
                    p.playSound(p.getLocation(), Sound.ANVIL_BREAK, 1, 1);
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

                ItemStack item = e.getItem();
                if (item != null && item.getType() == Material.FIREBALL && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    LargeFireball fireball = e.getClickedBlock().getWorld().spawn(e.getClickedBlock().getLocation().add(0.5, 1.5, 0.5), LargeFireball.class);
                    fireball.setDirection(new Vector(0, 0, 0));
                    fireball.setYield(3);
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
        if (e.getEntity() instanceof Villager && MG.core().getGame(e.getEntity().getWorld()) instanceof BedWarsGame) {
            if (e.getEntity().getLocation().getY() < 0) {
                e.getEntity().teleport(((BedWarsGame) MG.core().getGame(e.getEntity().getWorld())).villagerSpawnLocations.get(e.getEntity()));
            }
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

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent e) {
        Game g = MG.core().getGame(e.getPlayer());
        if (g instanceof BedWarsGame) {
            ItemStack itemStack = e.getItem().getItemStack();
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("RAW")) {
                meta.setDisplayName(null);
                itemStack.setItemMeta(meta);
                e.getItem().setItemStack(itemStack);
                if (itemStack.getType() == Material.IRON_INGOT) {
                    Team team = ((BedWarsGame) g).scoreboard.getEntryTeam(e.getPlayer().getName());
                    for (String entry : team.getEntries()) {
                        Player player = Bukkit.getPlayer(entry);
                        if (player != e.getPlayer() && g.players.contains(player)) {
                            player.getInventory().addItem(e.getItem().getItemStack());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPreferenceSelected(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getCustomName() != null) {
            TeamPreference preference = null;
            if (e.getRightClicked().getCustomName().contains("Solo")) {
                preference = TeamPreference.SOLO;
            } else if (e.getRightClicked().getCustomName().contains("Teams")) {
                preference = TeamPreference.TEAM;
            }

            if (preference != null) {
                e.getPlayer().sendMessage(ChatColor.GREEN + "Set your preference to " + e.getRightClicked().getCustomName());
                teamsPreference.put(e.getPlayer(), preference);
                preferenceArmorStandLocations.put(preference, e.getRightClicked().getLocation());

                if (MG.core().getQueue(e.getPlayer()) == null) {
                    MG.core().joinMinigameQueue(e.getPlayer(), getMinigameName());
                }
                updatePreferences();

                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onQueueJoin(QueueJoinEvent e) {
        if (e.getQueue() instanceof BedWars) {
            updatePreferences();
        }
    }

    @EventHandler
    public void onQueueJoin(QueueLeaveEvent e) {
        if (e.getQueue() instanceof BedWars) {
            updatePreferences();
        }
    }

    private void updatePreferences() {
        preferenceArmorStandLocations.forEach((preference, location) -> {
            ArmorStand armorStand = null;

            for (Entity e : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {
                if (e instanceof ArmorStand) {
                    armorStand = (ArmorStand) e;
                }
            }

            if (armorStand == null) {
                armorStand = location.getWorld().spawn(location, ArmorStand.class);
            }

            int count = 0;

            for (Player player : MG.core().getQueue(this).players) {
                if (teamsPreference.get(player) == preference) {
                    count ++;
                }
            }

            armorStand.setCustomName(ChatColor.RED.toString() + count + ChatColor.GOLD + " players prefer");
            armorStand.setCustomNameVisible(true);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.teleport(location.clone().add(0, 0.4, 0));
        });
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
        g.world.setGameRuleValue("naturalRegeneration", "false");

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

        List<Player> playersToAddToTeam = new ArrayList<>(g.players);

        for (int i = 0; i < g.teamCount; i++) {
            Location spawnLocation = spawnLocations.remove(0);
            ChatColor color = colors.remove(0);
            int colorData = colorDatas.remove(0);

            spawnLocation.setYaw(getAngleDegrees(spawnLocation));

            Team team = g.scoreboard.registerNewTeam(g.getColorName(color));
            team.setPrefix(color.toString());
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
            g.teamSpawnLocations.put(team, spawnLocation);
            g.teamBeds.put(team, spawnLocation.clone().add(-Math.sin(getAngle(spawnLocation)) * 2 + 0.5, 0, Math.cos(getAngle(spawnLocation)) * 2 + 0.5).getBlock());
            g.teamColors.put(team, color);
            g.teamColorDatas.put(team, (short) colorData);
            g.enchantments.put(team, new HashMap<>());

            for (Block bed : new Block[] {
                    spawnLocation.getBlock().getRelative((int) -Math.sin(getAngle(spawnLocation)) * 2, 0, (int) Math.cos(getAngle(spawnLocation)) * 2),
                    spawnLocation.getBlock().getRelative((int) -Math.sin(getAngle(spawnLocation)) * 3, 0, (int) Math.cos(getAngle(spawnLocation)) * 3),
            }) {
                g.beds.put(bed, new ColouredBed(bed, colorData));
            }

            int playersPerTeam = playersToAddToTeam.size() / (g.teamCount - i);
            if (playersToAddToTeam.size() == playersPerTeam) {
                playersToAddToTeam.forEach(player -> team.addEntry(player.getName()));
            } else {
                new PlayerGrouper(playersToAddToTeam, playersPerTeam, player -> {
                    team.addEntry(player.getName());
                    playersToAddToTeam.remove(player);
                });
            }

            bedAlive.getScore(g.getTeamName(team) + ChatColor.WHITE + ": " + ChatColor.GREEN + "❤").setScore(3);
        }

        for (Player player : g.players) {
            Team team = g.scoreboard.getEntryTeam(player.getName());

            MG.resetPlayer(player);

            player.teleport(g.teamSpawnLocations.get(team).clone().add(0.5, 0, 0.5));
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 1);
            g.minigame.message(player, "Game has started!");
            player.sendMessage("Destroy the other player's beds to stop them from respawning!");
            player.sendMessage("Buy gear from the villagers with iron and diamonds!");

            player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
            player.getEquipment().setChestplate(dye(Material.LEATHER_CHESTPLATE, g.teamColors.get(team)));
            player.getEquipment().setLeggings(dye(Material.LEATHER_LEGGINGS, g.teamColors.get(team)));

            player.setScoreboard(g.scoreboard);
            team.addEntry(player.getName());

            g.playerColours.put(player, g.teamColorDatas.get(team));
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

        updatePreferences();

        new GlowStoneFixer(this, g.world);
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

        int soloPreferences = 0;
        int teamPreferences = 0;

        for (Player p : g.players) {
            TeamPreference preference = teamsPreference.get(p);
            if (preference == TeamPreference.SOLO) {
                soloPreferences++;
            } else if (preference == TeamPreference.TEAM) {
                teamPreferences++;
            }
        }

        int minTeamSize = g.teamSize = (int) Math.ceil(g.players.size() / 8.0);

        if (g.players.size() >= 4 && teamPreferences > 0 && Math.random() * (soloPreferences + teamPreferences) < teamPreferences) {
            // Teams is preferred
            minTeamSize = 2;
        }

        for (int i = minTeamSize; i < g.players.size(); i++) {
            if (g.players.size() % i == 0) {
                g.teamSize = i;
                break;
            }
        }

        g.teamCount = (int) Math.ceil((double) g.players.size() / g.teamSize);

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
