package net.justminecraft.minigames.bedwars;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

public class MiscShop extends Shop {
    public MiscShop() {
        super("Geoff Walter", 3 * 9);

        setItem(11, new ShopItem(new ItemStack(Material.FIREBALL), emerald(6)));

        setItem(13, new ShopItem(invisPot(), emerald(6)));

        setItem(15, new ShopItem(new ItemStack(Material.OBSIDIAN, 7), emerald(8)));
    }

    private ItemStack invisPot() {
        return new ItemStack(Material.POTION, 1, (short) 16430);
    }
}
