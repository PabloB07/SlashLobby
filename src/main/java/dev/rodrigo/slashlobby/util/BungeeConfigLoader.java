package dev.rodrigo.slashlobby.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public abstract class BungeeConfigLoader {
    // This is the bungee version of util/VersionConfigLoader
    // It's literally the same, but for BungeeCord

    public static void init(Configuration config) {
        try {
            ConfigContainer.LOBBY_SERVER = config.getString("server", "lobby");
            ConfigContainer.DISABLED_SERVERS = config.getStringList("disabled_servers");
            ConfigContainer.COMMAND_ALIASES = config.getStringList("aliases");

            // When getting strings, they can be null
            // If we use the "def" param, we'll ensure it's never null
            ConfigContainer.SENDING_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("messages.sending", "&aSending you to the lobby..."));
            ConfigContainer.ALREADY_IN_LOBBY_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("messages.already_in_lobby", "&cYou are already in the lobby!"));

            ConfigContainer.DISABLED_SERVER_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("messages.disabled","&cThis command is disabled on this server!"));

            ConfigContainer.ERROR_CONSOLE_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("messages.error_console", "&cYou cannot execute this command in the console!"));

            ConfigContainer.INTERNAL_ERROR_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("messages.internal_error", "&cAn internal error has occurred."));
            ConfigContainer.ERROR_COOL_DOWN_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("messages.cooldown", "&cYou may use this command again in {time} seconds."));

            ConfigContainer.NO_PERMISSION_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("messages.no_permission", "&cYou don't have permission to execute this command."));

            ConfigContainer.CONFIG_RELOAD_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("messages.config_reload", "&aConfig reloaded!"));

            ConfigContainer.FORWARD_NO_PERMISSION = config.getBoolean("forward_no_permission");
            ConfigContainer.DELAY_COMMANDS = config.getBoolean("delay.enabled");

            ConfigContainer.DELAY_COMMANDS_UNIT = TimeUnit.valueOf(
                    config.getString("delay.unit", "SECONDS")
                            .toUpperCase()
            );

            ConfigContainer.DELAY_COMMANDS_VALUE = config.getInt("delay.value");

            ConfigContainer.TITLES_ENABLED = config.getBoolean("titles.enabled");

            ConfigContainer.TITLES_FADE_IN = config.getInt("titles.fadein");

            ConfigContainer.TITLES_FADEOUT = config.getInt("titles.fadeout");

            ConfigContainer.TITLES_STAY = config.getInt("titles.stay");

            ConfigContainer.TITLES_SENDING_TITLE = ChatColor.translateAlternateColorCodes('&', config.getString("titles.sending.title", "&aLobby"));

            ConfigContainer.TITLES_SENDING_SUBTITLE = ChatColor.translateAlternateColorCodes('&', config.getString("titles.sending.subtitle", "&aSending you to the lobby..."));

            ConfigContainer.TITLES_COOL_DOWN_TITLE = ChatColor.translateAlternateColorCodes('&', config.getString("titles.cooldown.title", "&aLobby"));

            ConfigContainer.TITLES_COOL_DOWN_SUBTITLE = ChatColor.translateAlternateColorCodes('&', config.getString("titles.cooldown.subtitle", "&aYou may use this command again in {time}."));

            ConfigContainer.TITLES_ALREADY_IN_LOBBY_TITLE = ChatColor.translateAlternateColorCodes('&', config.getString("titles.already_in_lobby.title", "&cLobby"));

            ConfigContainer.TITLES_ALREADY_IN_LOBBY_SUBTITLE = ChatColor.translateAlternateColorCodes('&', config.getString("titles.already_in_lobby.subtitle", "&cYou are already in the lobby!"));

            ConfigContainer.TITLES_DISABLED_TITLE = ChatColor.translateAlternateColorCodes('&', config.getString("titles.disabled.title", "&cLobby"));

            ConfigContainer.TITLES_DISABLED_SUBTITLE = ChatColor.translateAlternateColorCodes('&', config.getString("titles.disabled.subtitle", "&cThis command is disabled on this server!"));

            ConfigContainer.TITLES_INTERNAL_ERROR_TITLE = ChatColor.translateAlternateColorCodes('&', config.getString("titles.internal_error.title", "&cLobby"));

            ConfigContainer.TITLES_INTERNAL_ERROR_SUBTITLE = ChatColor.translateAlternateColorCodes('&', config.getString("titles.internal_error.subtitle", "&cAn internal error has occurred."));

            ConfigContainer.COOL_DOWN_ENABLED = config.getBoolean("cooldown.enabled");

            ConfigContainer.COOL_DOWN_UNIT = config.getString("cooldown.unit", "SECONDS");

            ConfigContainer.COOL_DOWN_VALUE = config.getInt("cooldown.value");
        } catch (Exception exception) {
            // If anything went wrong, print the stack trace
            throw new RuntimeException(exception);
        }
    }

}
