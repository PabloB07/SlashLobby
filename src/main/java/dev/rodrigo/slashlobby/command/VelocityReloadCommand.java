package dev.rodrigo.slashlobby.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import dev.rodrigo.slashlobby.SlashLobby;
import dev.rodrigo.slashlobby.util.ConfigContainer;

public class VelocityReloadCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        final CommandSource source = invocation.source();

        if (source.hasPermission(VelocityLobbyCommand.RELOAD_PERMISSION)) {
            // Reload the config
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
        }
    }

}
