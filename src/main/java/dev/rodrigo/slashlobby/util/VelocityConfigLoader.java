package dev.rodrigo.slashlobby.util;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public abstract class VelocityConfigLoader {
    // If we use the Velocity config loader, we'll get an error
    // If the user is using BungeeCord instead of Velocity
    // To avoid this, we load this class only if the user is using Velocity

    public static void init(CommentedConfigurationNode config) {
        try {
            ConfigContainer.LOBBY_SERVER = config.node("server").getString("lobby");
            ConfigContainer.DISABLED_SERVERS = config.node("disabled_servers").getList(TypeToken.get(String.class));
            ConfigContainer.COMMAND_ALIASES = config.node("aliases").getList(TypeToken.get(String.class));

            // When getting strings, they can be null
            // If we use the "def" param, we'll ensure it's never null
            ConfigContainer.SENDING_MESSAGE = (config.node("messages", "sending")
                    .getString("&aSending you to the lobby..."));
            ConfigContainer.ALREADY_IN_LOBBY_MESSAGE = (config.node("messages", "already_in_lobby")
                    .getString("&cYou are already in the lobby!"));

            ConfigContainer.DISABLED_SERVER_MESSAGE = (config.node("messages", "disabled")
                    .getString("&cThis command is disabled on this server!"));

            ConfigContainer.ERROR_CONSOLE_MESSAGE = (config.node("messages", "error_console")
                    .getString("&cYou cannot execute this command in the console!"));

            ConfigContainer.INTERNAL_ERROR_MESSAGE = (config.node("messages", "internal_error")
                    .getString("&cAn internal error has occurred."));
            ConfigContainer.ERROR_COOL_DOWN_MESSAGE = (config.node("messages", "cooldown")
                    .getString("&cYou may use this command again in {time} seconds."));

            ConfigContainer.NO_PERMISSION_MESSAGE = (config.node("messages" ,"no_permission")
                    .getString("&cYou don't have permission to execute this command."));

            ConfigContainer.CONFIG_RELOAD_MESSAGE = (config.node("messages", "config_reload")
                    .getString("&aConfig reloaded!"));

            ConfigContainer.FORWARD_NO_PERMISSION = config.node("forward_no_permission").getBoolean();
            ConfigContainer.DELAY_COMMANDS = config.node("delay", "commands")
                    .getBoolean();

            ConfigContainer.DELAY_COMMANDS_UNIT = TimeUnit.of(
                    ChronoUnit.valueOf(
                            config.node("delay", "unit")
                                    .getString("SECONDS")
                                    .toUpperCase()
                    )
            );

            ConfigContainer.DELAY_COMMANDS_VALUE = config.node("delay" ,"value")
                    .getInt();

            ConfigContainer.TITLES_ENABLED = config.node("titles", "enabled")
                    .getBoolean();

            ConfigContainer.TITLES_FADE_IN = config.node("titles", "fadein")
                    .getInt();

            ConfigContainer.TITLES_FADEOUT = config.node("titles", "fadeout")
                    .getInt();

            ConfigContainer.TITLES_STAY = config.node("titles", "stay")
                    .getInt();

            ConfigContainer.TITLES_SENDING_TITLE = (config.node("titles", "sending", "title")
                    .getString("&aLobby"));

            ConfigContainer.TITLES_SENDING_SUBTITLE = (config.node("titles", "sending", "subtitle")
                    .getString("&aSending you to the lobby..."));

            ConfigContainer.TITLES_COOL_DOWN_TITLE = (config.node("titles", "cooldown", "title")
                    .getString("&aLobby"));

            ConfigContainer.TITLES_COOL_DOWN_SUBTITLE = (config.node("titles", "cooldown", "subtitle")
                    .getString("&aYou may use this command again in {time}."));

            ConfigContainer.TITLES_ALREADY_IN_LOBBY_TITLE = (config.node("titles", "already_in_lobby", "title")
                    .getString("&cLobby"));

            ConfigContainer.TITLES_ALREADY_IN_LOBBY_SUBTITLE = (config.node("titles", "already_in_lobby", "subtitle")
                    .getString("&cYou are already in the lobby!"));

            ConfigContainer.TITLES_DISABLED_TITLE = (config.node("titles", "disabled", "title")
                    .getString("&cLobby"));

            ConfigContainer.TITLES_DISABLED_SUBTITLE = (config.node("titles", "disabled", "subtitle")
                    .getString("&cThis command is disabled on this server!"));

            ConfigContainer.TITLES_INTERNAL_ERROR_TITLE = (config.node("titles", "internal_error", "title")
                    .getString("&cLobby"));

            ConfigContainer.TITLES_INTERNAL_ERROR_SUBTITLE = (config.node("titles", "internal_error", "subtitle")
                    .getString("&cAn internal error has occurred."));

            ConfigContainer.COOL_DOWN_ENABLED = config.node("cooldown", "enabled")
                    .getBoolean();

            ConfigContainer.COOL_DOWN_UNIT = config.node("cooldown", "unit")
                    .getString("SECONDS");

            ConfigContainer.COOL_DOWN_VALUE = config.node("cooldown", "value")
                    .getInt();
        } catch (Exception exception) {
            // If anything went wrong, print the stack trace
            throw new RuntimeException(exception);
        }
    }
}
