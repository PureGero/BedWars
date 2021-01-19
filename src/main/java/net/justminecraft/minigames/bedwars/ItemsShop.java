package net.justminecraft.minigames.bedwars;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemsShop extends Shop {
    public ItemsShop(short color) {
        super("Items Shop", 4 * 9);

        setItem(10, new ShopItem(new ItemStack(Material.IRON_SWORD), iron(6), player -> replaceSword(player, Material.IRON_SWORD)));
        setItem(19, new ShopItem(new ItemStack(Material.DIAMOND_SWORD), diamond(4), player -> replaceSword(player, Material.DIAMOND_SWORD)));

        setItem(11, new ShopItem(lore(new ItemStack(Material.IRON_CHESTPLATE), ChatColor.GRAY + "Permanent armour that you will keep if you die"), iron(8), player -> replaceArmour(player, "IRON")));
        setItem(20, new ShopItem(lore(new ItemStack(Material.DIAMOND_CHESTPLATE), ChatColor.GRAY + "Permanent armour that you will keep if you die"), diamond(6), player -> replaceArmour(player, "DIAMOND")));

        setItem(12, new ShopItem(new ItemStack(Material.WOOL, 16, color), iron(4)));
        setItem(21, new ShopItem(new ItemStack(Material.STAINED_CLAY, 8, color), diamond(2)));

        setItem(13, new ShopItem(new ItemStack(Material.SHEARS), iron(6)));
        setItem(22, new ShopItem(new ItemStack(Material.DIAMOND_PICKAXE), diamond(4)));

        setItem(14, new ShopItem(new ItemStack(Material.GOLDEN_APPLE), iron(12)));
        setItem(23, new ShopItem(lore(new ItemStack(Material.TNT), ChatColor.GRAY + "TNT is automatically lit when placed, with a 2 second fuse"), diamond(4)));

        setItem(15, new ShopItem(new ItemStack(Material.ARROW, 8), iron(6)));
        setItem(24, new ShopItem(new ItemStack(Material.BOW), diamond(10)));

        setItem(16, new ShopItem(new ItemStack(Material.LADDER, 5), iron(4)));
        setItem(25, new ShopItem(new ItemStack(Material.ENDER_PEARL), diamond(4)));
    }

    private boolean replaceSword(Player player, Material sword) {
        int slot = getSlot(player, "SWORD");

        if (slot == -1) {
            player.getInventory().addItem(new ItemStack(sword));
        } else {
            Material mat = player.getInventory().getItem(slot).getType();
            if ((mat == Material.IRON_SWORD || mat == Material.DIAMOND_SWORD) && sword == Material.IRON_SWORD) {
                return false;
            } else if (mat == Material.DIAMOND_SWORD && sword == Material.DIAMOND_SWORD) {
                return false;
            }

            player.getInventory().setItem(slot, new ItemStack(sword));
        }

        UpgradesShop.updateEnchants(player);

        return true;
    }

    private boolean replaceArmour(Player player, String type) {
        Material mat = player.getEquipment().getChestplate() == null ? Material.AIR : player.getEquipment().getChestplate().getType();

        if ((mat.name().contains("IRON") || mat.name().contains("DIAMOND")) && type.equals("IRON")) {
            return false;
        } else if (mat.name().contains("DIAMOND") && type.equals("DIAMOND")) {
            return false;
        }

        player.getEquipment().setChestplate(new ItemStack(Material.valueOf(type + "_CHESTPLATE")));
        player.getEquipment().setLeggings(new ItemStack(Material.valueOf(type + "_LEGGINGS")));
        player.getEquipment().setBoots(new ItemStack(Material.valueOf(type + "_BOOTS")));

        UpgradesShop.updateEnchants(player);

        return true;
    }

    private int getSlot(Player player, String search) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getType().name().contains(search)) {
                return i;
            }
        }
        return -1;
    }
}
