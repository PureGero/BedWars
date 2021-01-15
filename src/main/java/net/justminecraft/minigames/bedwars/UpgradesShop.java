package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.HashMap;

public class UpgradesShop extends Shop {
    public UpgradesShop(Player player) {
        super("Upgrades Shop", 3 * 9);

        Game game = MG.core().getGame(player);
        updateItems((BedWarsGame) game, player.getScoreboard().getEntryTeam(player.getName()));
    }

    private void updateItems(BedWarsGame game, Team team) {
        int level;

        level = game.enchantments.get(team).getOrDefault(Enchantment.DAMAGE_ALL, 0);
        setItem(10, new ShopItem(upgrade(Material.DIAMOND_SWORD, Enchantment.DAMAGE_ALL, "Sharpness", level), emerald(level * 2 + 2), player -> upgrade(player, Enchantment.DAMAGE_ALL)));

        level = game.enchantments.get(team).getOrDefault(Enchantment.PROTECTION_ENVIRONMENTAL, 0);
        setItem(12, new ShopItem(upgrade(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION_ENVIRONMENTAL, "Protection", level), emerald(level * 2 + 2), player -> upgrade(player, Enchantment.PROTECTION_ENVIRONMENTAL)));

        level = game.enchantments.get(team).getOrDefault(Enchantment.ARROW_KNOCKBACK, 0);
        setItem(14, new ShopItem(upgrade(Material.BOW, Enchantment.ARROW_KNOCKBACK, "Punch", level), emerald(level * 2 + 2), player -> upgrade(player, Enchantment.ARROW_KNOCKBACK)));

        level = game.enchantments.get(team).getOrDefault(Enchantment.LURE, 0);
        setItem(16, new ShopItem(upgrade(Material.FURNACE, Enchantment.LURE, "More Iron", level), emerald(level * 2 + 2), player -> upgrade(player, Enchantment.LURE)));
    }

    private ItemStack upgrade(Material material, Enchantment enchantment, String name, int level) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchantment, level + 1, true);
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Upgrades are applied to the entire team",
                "",
                (level >= 1 ? ChatColor.GREEN : ChatColor.RED) + name + " Tier I: 2 emeralds",
                (level >= 2 ? ChatColor.GREEN : ChatColor.RED) + name + " Tier II: 4 emeralds",
                (level >= 3 ? ChatColor.GREEN : ChatColor.RED) + name + " Tier III: 6 emeralds"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private boolean upgrade(Player player, Enchantment enchantment) {
        Game g = MG.core().getGame(player);
        if (g instanceof BedWarsGame) {
            Team team = player.getScoreboard().getEntryTeam(player.getName());
            HashMap<Enchantment, Integer> enchants = ((BedWarsGame) g).enchantments.computeIfAbsent(team, k -> new HashMap<>());
            if (enchants.getOrDefault(enchantment, 0) >= 3) {
                return false;
            }

            enchants.put(enchantment, enchants.getOrDefault(enchantment, 0) + 1);

            g.players.forEach(UpgradesShop::updateEnchants);

            updateItems((BedWarsGame) g, team);

            return true;
        }

        return false;
    }

    public static void updateEnchants(Player player) {
        Game g = MG.core().getGame(player);
        if (g instanceof BedWarsGame) {
            Team team = player.getScoreboard().getEntryTeam(player.getName());
            HashMap<Enchantment, Integer> enchants = ((BedWarsGame) g).enchantments.get(team);
            if (enchants != null) {
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (updateEnchant(item, enchants)) {
                        player.getInventory().setItem(i, item);
                    }
                }
                ItemStack item = player.getEquipment().getHelmet();
                if (updateEnchant(item, enchants)) {
                    player.getEquipment().setHelmet(item);
                }
                item = player.getEquipment().getChestplate();
                if (updateEnchant(item, enchants)) {
                    player.getEquipment().setChestplate(item);
                }
                item = player.getEquipment().getLeggings();
                if (updateEnchant(item, enchants)) {
                    player.getEquipment().setLeggings(item);
                }
                item = player.getEquipment().getBoots();
                if (updateEnchant(item, enchants)) {
                    player.getEquipment().setBoots(item);
                }
            }
        }
    }

    private static boolean updateEnchant(ItemStack item, HashMap<Enchantment, Integer> enchants) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (item.getType().name().contains("HELMET") || item.getType().name().contains("CHESTPLATE") || item.getType().name().contains("LEGGINGS") || item.getType().name().contains("BOOTS")) {
            if (enchants.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL) && meta.getEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) != enchants.get(Enchantment.PROTECTION_ENVIRONMENTAL)) {
                meta.removeEnchant(Enchantment.PROTECTION_ENVIRONMENTAL);
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, enchants.get(Enchantment.PROTECTION_ENVIRONMENTAL), true);
                item.setItemMeta(meta);
                return true;
            }
        }

        if (item.getType().name().contains("SWORD")) {
            if (enchants.containsKey(Enchantment.DAMAGE_ALL) && meta.getEnchantLevel(Enchantment.DAMAGE_ALL) != enchants.get(Enchantment.DAMAGE_ALL)) {
                meta.removeEnchant(Enchantment.DAMAGE_ALL);
                meta.addEnchant(Enchantment.DAMAGE_ALL, enchants.get(Enchantment.DAMAGE_ALL), true);
                item.setItemMeta(meta);
                return true;
            }
        }

        if (item.getType().name().contains("BOW")) {
            if (enchants.containsKey(Enchantment.ARROW_KNOCKBACK) && meta.getEnchantLevel(Enchantment.ARROW_KNOCKBACK) != enchants.get(Enchantment.ARROW_KNOCKBACK)) {
                meta.removeEnchant(Enchantment.ARROW_KNOCKBACK);
                meta.addEnchant(Enchantment.ARROW_KNOCKBACK, enchants.get(Enchantment.ARROW_KNOCKBACK), true);
                item.setItemMeta(meta);
                return true;
            }
        }

        return false;
    }
}
