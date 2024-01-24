package dev.rodrigo.slashlobby.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CoolDownCacheStorage {
    // Storage for all usages of cool downs
    // Define this as a global class which don't use any Bungee or Velocity API
    // This way we can use it in both BungeeCord and Velocity

    // Both proxy APIs provide the player's UUID, this way we can store it
    // Without even touching their API from here
    public static Map<UUID, Long> COOL_DOWN_CACHE = new ConcurrentHashMap<>();

    // Convert the coo down to milliseconds
    // This way we can easily compare it with the current time
     static int COOL_DOWN_REGISTERED_TIME;

    public static boolean IS_ENABLED;

    public static void init() {
        // Get the cool down unit
        // This way we can easily convert it to milliseconds
        // Supported Units: SECONDS, MINUTES, HOURS, DAYS
        int baseMs;
        switch (ConfigContainer.COOL_DOWN_UNIT) {
            case "MINUTES":
                baseMs = 1000 * 60;
                break;
            case "HOURS":
                baseMs = 1000 * 60 * 60;
                break;
            case "DAYS":
                baseMs = 1000 * 60 * 60 * 24;
                break;
            default:
                baseMs = 1000;
                break;
        }

        IS_ENABLED = true;

        // Multiply the value by the unit to get the cool down in milliseconds
        COOL_DOWN_REGISTERED_TIME = baseMs * ConfigContainer.COOL_DOWN_VALUE;
    }

    public static void registerUsage(UUID uuid) {
        if (!IS_ENABLED) return;
        // Register a usage
        COOL_DOWN_CACHE.put(uuid, System.currentTimeMillis());
    }

    public static long getTimeElapsedSinceLastUsage(UUID uuid) {
        // Check if a player can use the command again
        return !COOL_DOWN_CACHE.containsKey(uuid) ? -1 :  System.currentTimeMillis() - COOL_DOWN_CACHE.get(uuid);
    }

    public static void deleteUsage(UUID uuid) {
        COOL_DOWN_CACHE.remove(uuid);
    }

    public static int getCoolDownRegisteredTime() {
        return COOL_DOWN_REGISTERED_TIME;
    }
}
