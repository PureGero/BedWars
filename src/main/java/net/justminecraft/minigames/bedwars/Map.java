package net.justminecraft.minigames.bedwars;

import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public enum Map {
    BEDWARS(
            51,
            new Location[]{
                    new Location(null, -10, 68, -10),
                    new Location(null, 10, 68, 10)
            }
    ),
    MUSHROOM(
            51,
            new Location[]{
                    new Location(null, 0, 67, -6),
                    new Location(null, 0, 67, 4)
            }
    );

    private final int distance;
    private final Location[] emeraldSpawnPoints;

    Map(int distance, Location[] emeraldSpawnPoints) {
        this.distance = distance;
        this.emeraldSpawnPoints = emeraldSpawnPoints;
    }

    public int getDistance() {
        return distance;
    }

    public Location[] getEmeraldSpawnPoints() {
        return emeraldSpawnPoints;
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
