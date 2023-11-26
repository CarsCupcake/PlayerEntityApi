package me.carscupcake.playerentityapi.nms;

import me.carscupcake.playerentityapi.Main;
import me.carscupcake.playerentityapi.utils.FieldAccessor;
import me.carscupcake.playerentityapi.utils.NMSUtils;
import me.carscupcake.playerentityapi.utils.ReflectionUtils;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * @author CarsCupcake
 */
public class NMSConnection {
    private static volatile FieldAccessor NETWORK_ACCESSOR;
    private final Object nmsPlayer;
    private final Object connection;
    public NMSConnection(Object nmsPlayer){
        this.nmsPlayer = nmsPlayer;
        if(Main.VERSION >= 8 && Main.VERSION <= 16)
           connection = ReflectionUtils.getField(Objects.requireNonNull(ReflectionUtils.findField(nmsPlayer.getClass(), "playerConnection")), nmsPlayer);
        else if(Main.VERSION >= 17)
            connection = ReflectionUtils.getField(Objects.requireNonNull(ReflectionUtils.findField(nmsPlayer.getClass(), "b")), nmsPlayer);
        NMSUtils.getPlayerConnectionClass()
    }
    private Object getNetworkManager(Player player) {
        if (NETWORK_ACCESSOR == null) {
            Class<?> networkClass = NMSUtils.getNetworkManagerClass();
            Class<?> connectionClass = NMSUtils.getPlayerConnectionClass();
            NETWORK_ACCESSOR = new FieldAccessor<>(connectionClass, networkClass);
        }
    }
}
