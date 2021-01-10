package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
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
    public UpgradesShop() {
        super("Upgrades Shop", 3 * 9);

        setItem(10, new ShopItem(upgrade(Material.DIAMOND_SWORD, Enchantment.DAMAGE_ALL, "Sharpness"), emerald(2), player -> upgrade(player, Enchantment.DAMAGE_ALL)));
        setItem(12, new ShopItem(upgrade(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION_ENVIRONMENTAL, "Protection"), emerald(4), player -> upgrade(player, Enchantment.PROTECTION_ENVIRONMENTAL)));
        setItem(14, new ShopItem(upgrade(Material.BOW, Enchantment.ARROW_KNOCKBACK, "Punch"), emerald(3), player -> upgrade(player, Enchantment.ARROW_KNOCKBACK)));
        setItem(16, new ShopItem(upgrade(Material.FURNACE, Enchantment.LURE, "More Iron"), emerald(6), player -> upgrade(player, Enchantment.LURE)));
    }

    private ItemStack upgrade(Material material, Enchantment enchantment, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchantment, 1, true);
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(
                "",
                name + " Tier I: X emeralds",
                name + " Tier II: Y emeralds",
                name + " Tier III: Z emeralds"
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
