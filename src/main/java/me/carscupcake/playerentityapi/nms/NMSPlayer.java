package me.carscupcake.playerentityapi.nms;

import me.carscupcake.playerentityapi.utils.ReflectionUtils;
import org.bukkit.entity.Player;

/**
 * @author CarsCupcake
 */
public class NMSPlayer {
    private final Player player;
    private final Object nmsPlayer;
    public NMSPlayer(Player player){
        this.player = player;
        nmsPlayer = ReflectionUtils.invokeMethod(ReflectionUtils.findMethod(player.getClass(), "getHandle"), player);
    }
    public NMSConnection connection(){

        return null;
    }
}
