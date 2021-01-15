package net.justminecraft.minigames.bedwars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Shop implements InventoryHolder {

    private final Inventory inventory;
    private final HashMap<Integer, ShopItem> items = new HashMap<>();

    public Shop(String name, int size) {
        inventory = Bukkit.createInventory(this, size, name);

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack placeholder = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 8);
            ItemMeta meta = placeholder.getItemMeta();
            meta.setDisplayName(" ");
            placeholder.setItemMeta(meta);
            inventory.setItem(i, placeholder);
        }
    }

    public Shop setItem(int i, ShopItem item) {
        items.put(i, item);

        ItemStack clickable = lore(
                new ItemStack(item.getItem()),
                "",
                ChatColor.GOLD + "Cost: " + item.getCost().getAmount() + " " + readable(item.getCost().getType().name()) + (item.getCost().getAmount() == 1 ? "" : "s")
        );
        inventory.setItem(i, clickable);

        return this;
    }

    public static String readable(String name) {
        return name.replaceAll("_", " ").toLowerCase();
    }

    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (e.getClickedInventory().getHolder() != this) {
            return;
        }

        if (!items.containsKey(e.getSlot())) {
            return;
        }

        items.get(e.getSlot()).tryPurchase((Player) e.getWhoClicked());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public ItemStack iron(int amount) {
        return new ItemStack(Material.IRON_INGOT, amount);
    }

    public ItemStack diamond(int amount) {
        return new ItemStack(Material.DIAMOND, amount);
    }

    public ItemStack emerald(int amount) {
        return new ItemStack(Material.EMERALD, amount);
    }

    public ItemStack lore(ItemStack itemStack, String... loreToAdd) {
        ItemMeta meta = itemStack.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();

        if (meta.getLore() != null) {
            lore.addAll(meta.getLore());
        }

        lore.addAll(Arrays.asList(loreToAdd));
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
