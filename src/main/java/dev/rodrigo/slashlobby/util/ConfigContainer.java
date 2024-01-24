package dev.rodrigo.slashlobby.util;

import dev.rodrigo.slashlobby.SlashLobby;
import dev.rodrigo.slashlobby.bungee.LobbyBungee;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class ConfigContainer {
    // This is a utility class
    // It will store the config values
    // This way we don't read it everytime we need it
    // Why make this in a global object?
    // Because we can use this with Velocity and BungeeCord
    // Without creating a specific object for each plugin

    // - Lobby Server
    public static String LOBBY_SERVER;
    // - Servers
    public static List<String> DISABLED_SERVERS;
    // - Aliases
    public static List<String> COMMAND_ALIASES;
    // - Messages
    public static String SENDING_MESSAGE;
    public static String ALREADY_IN_LOBBY_MESSAGE;
    public static String DISABLED_SERVER_MESSAGE;
    public static String ERROR_CONSOLE_MESSAGE;
    public static String INTERNAL_ERROR_MESSAGE;
    public static String ERROR_COOL_DOWN_MESSAGE;
    public static String NO_PERMISSION_MESSAGE;
    public static String CONFIG_RELOAD_MESSAGE;

    // - Forwarding on No Permission
    public static boolean FORWARD_NO_PERMISSION;

    // - Cool-down
    public static boolean COOL_DOWN_ENABLED;
    public static String COOL_DOWN_UNIT;
    public static int COOL_DOWN_VALUE;

    // - Delay commands
    public static boolean DELAY_COMMANDS;
    public static TimeUnit DELAY_COMMANDS_UNIT;
    public static int DELAY_COMMANDS_VALUE;

    // - Titles
    public static boolean TITLES_ENABLED;
    public static int TITLES_FADE_IN;
    public static int TITLES_STAY;
    public static int TITLES_FADEOUT;
    public static String TITLES_SENDING_TITLE;
    public static String TITLES_SENDING_SUBTITLE;
    public static String TITLES_COOL_DOWN_TITLE;
    public static String TITLES_COOL_DOWN_SUBTITLE;
    public static String TITLES_ALREADY_IN_LOBBY_TITLE;
    public static String TITLES_ALREADY_IN_LOBBY_SUBTITLE;
    public static String TITLES_DISABLED_TITLE;
    public static String TITLES_DISABLED_SUBTITLE;
    public static String TITLES_INTERNAL_ERROR_TITLE;
    public static String TITLES_INTERNAL_ERROR_SUBTITLE;

    // As the configuration loaders for BungeeCord and Velocity are different, we need to initialize them
    // In a different way, we'll use this method to initialize them for Velocity
    public static void init(SlashLobby velocityPlugin) {
        VelocityConfigLoader.init(velocityPlugin.getConfig());
    }

    public static void init(LobbyBungee bungeePlugin) {
        BungeeConfigLoader.init(bungeePlugin.getConfig());
    }
}
