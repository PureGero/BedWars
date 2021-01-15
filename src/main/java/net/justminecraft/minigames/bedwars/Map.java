package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public enum Map {
    BEDWARS(
            51,
            new Vector(0, 2, -14),
            new Vector(-5, 0, -9),
            new Vector(5, 0, -9),
            new Vector[] {
                    new Vector(-10, 6, -10),
                    new Vector(10, 6, 10)
            },
            new Vector(0, -3, 0)
    ),
    MUSHROOM(
            51,
            new Vector(0, 2, -13),
            new Vector(-3, 0, -9),
            new Vector(3, 0, -9),
            new Vector[] {
                    new Vector(0, 5, -6),
                    new Vector(0, 5, 4)
            },
            new Vector(1, 20, -1)
    );

    private final int distance;
    private final Vector ironVector;
    private final Vector shopVector;
    private final Vector upgradeVector;
    private final Vector[] emeraldVectors;
    private final Vector miscVector;

    Map(int distance, Vector ironVector, Vector shopVector, Vector upgradeVector, Vector[] emeraldVectors, Vector miscVector) {
        this.distance = distance;
        this.ironVector = ironVector;
        this.shopVector = shopVector;
        this.upgradeVector = upgradeVector;
        this.emeraldVectors = emeraldVectors;
        this.miscVector = miscVector;
    }

    public int getDistance() {
        return distance;
    }

    public Vector getIronVector() {
        return ironVector;
    }

    public Vector getShopVector() {
        return shopVector;
    }

    public Vector getUpgradeVector() {
        return upgradeVector;
    }

    public Vector[] getEmeraldVectors() {
        return emeraldVectors;
    }

    public Vector getMiscVector() {
        return miscVector;
    }

    public void placeSchematic(WorldBuffer w, Location l, String key) {
        placeSchematic(w, l, key, 0);
    }

    public void placeSchematic(WorldBuffer w, Location l, String key, int angle) {
        placeSchematic(w, l, key, angle, 0);
    }

    public void placeSchematic(WorldBuffer w, Location l, String key, int angle, int color) {
        File schem = new File(BedWars.DATA_FOLDER, "schematics/" + name().toLowerCase() + "_" + key + "_" + angle + ".schematic");

        if (schem.isFile()) {
            HashMap<Material, ArrayList<Location>> query = w.placeSchematic(l, schem, Material.WOOL);
            query.get(Material.WOOL).forEach(wool -> w.setBlockAt(wool, Material.WOOL, (byte) color));
        }
    }
}
