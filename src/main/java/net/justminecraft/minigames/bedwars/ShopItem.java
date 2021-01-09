package net.justminecraft.minigames.bedwars;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public class ShopItem {

    private final ItemStack item;
    private final ItemStack cost;
    private final Function<Player, Boolean> onPurchase;

    public ShopItem(ItemStack item, ItemStack cost) {
        this(item, cost, player -> {
            player.getInventory().addItem(item);
            return true;
        });
    }

    public ShopItem(ItemStack item, ItemStack cost, Function<Player, Boolean> onPurchase) {
        this.item = item;
        this.cost = cost;
        this.onPurchase = onPurchase;
    }

    public void tryPurchase(Player player) {
        if (!canPay(player)) {
            player.sendMessage(ChatColor.RED + "Not enough " + Shop.readable(cost.getType().name()) + "s!");
            return;
        }

        if (!onPurchase.apply(player)) {
            player.sendMessage(ChatColor.RED + "You already have that item or better!");
            return;
        }

        takePay(player);

        player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1f, 2f);
    }

    private boolean canPay(Player player) {
        int count = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == cost.getType()) {
                count += item.getAmount();
            }
        }

        return count >= cost.getAmount();
    }

    private void takePay(Player player) {
        int amount = cost.getAmount();

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() == cost.getType()) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                    amount = 0;
                    player.getInventory().setItem(i, item);
                } else {
                    amount -= item.getAmount();
                    player.getInventory().setItem(i, null);
                }
            }

            if (amount == 0) {
                break;
            }
        }
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemStack getCost() {
        return cost;
    }
}
