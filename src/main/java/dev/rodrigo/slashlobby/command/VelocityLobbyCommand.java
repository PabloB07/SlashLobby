package dev.rodrigo.slashlobby.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.rodrigo.slashlobby.SlashLobby;
import dev.rodrigo.slashlobby.util.ConfigContainer;

public class VelocityLobbyCommand implements SimpleCommand {
    static final String RELOAD_PERMISSION = "slashlobby.reload";

    @Override
    public void execute(Invocation invocation) {
        // Get the arguments since the reload command is "/lobby reload"
        final String[] args = invocation.arguments();
        final CommandSource source = invocation.source();

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // Check if the sender has the correct permission
            if (source.hasPermission(RELOAD_PERMISSION)) {
                // Reload the config
                try {
                    SlashLobby.instance.reInitConfig();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                ConfigContainer.init(SlashLobby.instance);
                source.sendMessage(
                        SlashLobby.messageColor.deserialize(
                                SlashLobby.replacePlaceholders(
                                        ConfigContainer.CONFIG_RELOAD_MESSAGE,
                                        source
                                )
                        )
                );
            } else if (!ConfigContainer.FORWARD_NO_PERMISSION) {
                // Check if the user has the option "forward-no-permission" to true
                // If so, forward the player into the lobby anyway
                // Send the no permission message
                source.sendMessage(
                        SlashLobby.messageColor.deserialize(
                                SlashLobby.replacePlaceholders(
                                        ConfigContainer.NO_PERMISSION_MESSAGE,
                                        source
                                )
                        )
                );
                return;
            }
        }

        // Check if the sender is a player
        if (source instanceof Player) {
            // Create a connection request to the lobby
            SlashLobby.createConnectionRequest((Player) source);
            return;
        }

        // Send the error message if the sender is not a player
        source.sendMessage(
                SlashLobby.messageColor.deserialize(
                        SlashLobby.replacePlaceholders(
                                ConfigContainer.ERROR_CONSOLE_MESSAGE,
                                source
                        )
                )
        );
    }

}
