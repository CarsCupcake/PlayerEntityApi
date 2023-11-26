package me.carscupcake.playerentityapi.utils;

import me.carscupcake.playerentityapi.Main;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Some code taken by ProtocolLib
public class NMSUtils {

    private static final ClassSource CLASS_SOURCE = ClassSource.fromClassLoader();
    static CachedPackage minecraftPackage;
    /**
     * Represents a regular expression that will match the version string in a package: org.bukkit.craftbukkit.v1_6_R2 ->
     * v1_6_R2
     */
    private static final Pattern PACKAGE_VERSION_MATCHER = Pattern.compile(".*\\.(v\\d+_\\d+_\\w*\\d+)");
    private static String packageVersion;
    private static String MINECRAFT_PREFIX_PACKAGE = "net.minecraft.server";
    private static String MINECRAFT_FULL_PACKAGE = null;
    private static String CRAFTBUKKIT_PACKAGE = null;
    private static final String CANONICAL_REGEX = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)+\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final String MINECRAFT_CLASS_NAME_REGEX = "net\\.minecraft\\." + CANONICAL_REGEX;

    /**
     * Regular expression computed dynamically.
     */
    private static String DYNAMIC_PACKAGE_MATCHER = null;

    /**
     * The Entity package in Forge 1.5.2
     */
    private static final String FORGE_ENTITY_PACKAGE = "net.minecraft.entity";
    private NMSUtils(){}

    public static void init() {
        try {
            // get the bukkit version we're running on
            Server craftServer = Bukkit.getServer();
            CRAFTBUKKIT_PACKAGE = craftServer.getClass().getPackage().getName();

            // Parse the package version
            Matcher packageMatcher = PACKAGE_VERSION_MATCHER.matcher(CRAFTBUKKIT_PACKAGE);
            if (packageMatcher.matches()) {
                packageVersion = packageMatcher.group(1);
            } else {
                // Just assume R1 - it's probably fine (warn anyway)
                packageVersion = "v" + Main.VERSION + "_" + Main.PATCH + "_R1";
                Main.getMain().getLogger().log(Level.SEVERE, "Assuming package version: " + packageVersion);
            }

            if (Main.VERSION >= 17) {
                // total rework of the NMS structure in 1.17 (at least there's no versioning)
                MINECRAFT_FULL_PACKAGE = MINECRAFT_PREFIX_PACKAGE = "net.minecraft";
                setDynamicPackageMatcher(MINECRAFT_CLASS_NAME_REGEX);
            } else {
                // extract the server version from the return type of "getHandle" in CraftEntity
                Method getHandle = getCraftEntityClass().getMethod("getHandle");
                MINECRAFT_FULL_PACKAGE = getHandle.getReturnType().getPackage().getName();

                // Pretty important invariant
                if (!MINECRAFT_FULL_PACKAGE.startsWith(MINECRAFT_PREFIX_PACKAGE)) {
                    // See if we got the Forge entity package
                    if (MINECRAFT_FULL_PACKAGE.equals(FORGE_ENTITY_PACKAGE)) {
                        // Use the standard NMS versioned package
                        MINECRAFT_FULL_PACKAGE = MINECRAFT_PREFIX_PACKAGE + "." + packageVersion;
                    } else {
                        // Assume they're the same instead
                        MINECRAFT_PREFIX_PACKAGE = MINECRAFT_FULL_PACKAGE;
                    }

                    // The package is usually flat, so go with that assumption
                    String matcher =
                            (!MINECRAFT_PREFIX_PACKAGE.isEmpty() ? Pattern.quote(MINECRAFT_PREFIX_PACKAGE + ".") : "") + CANONICAL_REGEX;

                    // We'll still accept the default location, however
                    setDynamicPackageMatcher("(" + matcher + ")|(" + MINECRAFT_CLASS_NAME_REGEX + ")");

                } else {
                    // Use the standard matcher
                    setDynamicPackageMatcher(MINECRAFT_CLASS_NAME_REGEX);
                }
            }
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Cannot find getHandle() in CraftEntity", exception);
        }
    }
    private static void setDynamicPackageMatcher(String regex) {
        DYNAMIC_PACKAGE_MATCHER = regex;
    }
    public static Class<?> getCraftEntityClass() {
        return getCraftBukkitClass("entity.CraftEntity");
    }
    public static Class<?> getCraftBukkitClass(String s){
        try {
            return Class.forName(getCraftBukkitPackage() + "." + s);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public static String getCraftBukkitPackage() {
        // Ensure it has been initialized
        if (CRAFTBUKKIT_PACKAGE == null) {
            return MINECRAFT_FULL_PACKAGE;
        }

        return CRAFTBUKKIT_PACKAGE;
    }
    private static Optional<Class<?>> getOptionalNMS(String className, String... aliases) {
        if (MINECRAFT_FULL_PACKAGE == null) {
            minecraftPackage = new CachedPackage(MINECRAFT_FULL_PACKAGE, CLASS_SOURCE);
        }

        return minecraftPackage.getPackageClass(className, aliases);
    }
    public static Class<?> getMinecraftClass(String className, String... aliases) {
        return getOptionalNMS(className, aliases)
                .orElseThrow(() -> new RuntimeException(String.format("Unable to find %s (%s)", className, String.join(", ", aliases))));
    }
    public static Class<?> getNetworkManagerClass() {
        return getMinecraftClass("network.NetworkManager", "network.Connection", "NetworkManager");
    }
    public static Class<?> getPlayerConnectionClass() {
        return getMinecraftClass("server.network.PlayerConnection", "server.network.ServerGamePacketListenerImpl", "PlayerConnection");
    }
}
