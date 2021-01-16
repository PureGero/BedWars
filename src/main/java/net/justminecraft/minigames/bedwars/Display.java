package net.justminecraft.minigames.bedwars;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public abstract class Display<K, V> {

    private final Location location;
    private final int count;
    private final String title;
    private final String suffix;

    public Display(Location location, int count, String title, String suffix) {
        this.location = location;
        this.count = count;
        this.title = title;
        this.suffix = suffix;
    }

    public abstract DisplayEntry<K, V> getEntry(int i);

    public void refresh() {
        setArmourStand(-1, ChatColor.YELLOW.toString() + ChatColor.BOLD + title);
        for (int i = 0; i < count; i++) {
            DisplayEntry<K, V> entry = getEntry(i);
            setArmourStand(i, ChatColor.YELLOW.toString() + (i + 1) + ". "
                    + ChatColor.GOLD + entry.getKey() + ChatColor.GRAY + " - "
                    + ChatColor.YELLOW + entry.getValue() + suffix);
        }
    }

    private void setArmourStand(int i, String name) {
        Location loc = location.clone().add(0, (count - i) * 0.4, 0);
        ArmorStand armorStand = null;

        for (Entity e : loc.getChunk().getEntities()) {
            if (e instanceof ArmorStand && e.getLocation().distanceSquared(loc) < 0.1 * 0.1) {
                armorStand = (ArmorStand) e;
            }
        }

        if (armorStand == null) {
            armorStand = loc.getWorld().spawn(loc, ArmorStand.class);
        }

        if (!name.equals(armorStand.getCustomName())) {
            armorStand.teleport(loc);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(name);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
        }
    }

    public static class DisplayEntry<K, V> {
        private K key;
        private V value;

        public DisplayEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
