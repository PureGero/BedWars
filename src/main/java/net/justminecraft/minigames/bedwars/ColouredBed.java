package net.justminecraft.minigames.bedwars;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;

public class ColouredBed {
    private final Location location;
    private final int id;

    public ColouredBed(Block block, int color) {
        location = block.getLocation();
        //noinspection deprecation
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
                PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_13.BLOCK_CHANGE, Via.getManager().getConnectionManager().getConnectedClient(player.getUniqueId()));
                packet.write(Type.POSITION1_8, new Position(location.getBlockX(), (short) location.getBlockY(), location.getBlockZ()));
                packet.write(Type.VAR_INT, id);
                packet.send(Protocol1_13To1_12_2.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
