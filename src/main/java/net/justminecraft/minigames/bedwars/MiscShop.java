package net.justminecraft.minigames.bedwars;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MiscShop extends Shop {
    public MiscShop() {
        super("Geoff Walter", 3 * 9);

        setItem(12, new ShopItem(new ItemStack(Material.FIREBALL), emerald(6)));

        setItem(14, new ShopItem(new ItemStack(Material.OBSIDIAN, 7), emerald(8)));
    }
}
