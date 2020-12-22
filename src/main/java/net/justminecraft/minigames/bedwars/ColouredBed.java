package net.justminecraft.minigames.bedwars;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;

public class ColouredBed {
    private final Location location;
    private final int id;

    public ColouredBed(Block block, int color) {
        location = block.getLocation();
        id = getBedId(block.getData(), color);
    }

    private static final int[] FACING_MAP = {
            1,
            2,
            0,
            3
    };

    private int getBedId(byte data, int color) {
        int facing = data & 0b11;
        int occupied = (data >> 2) & 0b1;
        int head = (data >> 3) & 0b1;

        return (color << 4
                | FACING_MAP[facing] << 2
                | (occupied ^ 1) << 1
                | head ^ 1)
                + 748;
    }

    public void send(Player player) {
        try {
            if (Via.getAPI().getPlayerVersion(player.getUniqueId()) >= ProtocolVersion.v1_13.getVersion()) {
                PacketWrapper packet = new PacketWrapper(0x0B, null, Via.getManager().getConnection(player.getUniqueId()));
                packet.write(Type.POSITION, new Position(location.getBlockX(), (short) location.getBlockY(), location.getBlockZ()));
                packet.write(Type.VAR_INT, id);
                packet.send(Protocol1_13To1_12_2.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
