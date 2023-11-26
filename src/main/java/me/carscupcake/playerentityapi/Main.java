package me.carscupcake.playerentityapi;

import lombok.Getter;
import me.carscupcake.playerentityapi.utils.NMSUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Pattern;

/**
 * @author CarsCupcake
 */
public final class Main extends JavaPlugin {
    @Getter
    private static Main main;
    public static int VERSION;
    public static int PATCH;

    @Override
    public void onEnable() {
        main = this;
        VERSION = getMinorVersion();
        PATCH = getPatchVersion();
        NMSUtils.init();
        getLogger().info("Loading on Mc Minor Version " + VERSION);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    private int getMinorVersion() {
        return Integer.parseInt(main.getServer().getVersion().split("\\.")[1]);
    }
    private int getPatchVersion() {
        String s = main.getServer().getVersion().split("\\.")[2];
        return Integer.parseInt(s.substring(0, s.indexOf("-")));
    }
}
