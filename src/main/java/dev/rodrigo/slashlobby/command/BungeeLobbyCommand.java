package dev.rodrigo.slashlobby.command;

import dev.rodrigo.slashlobby.SlashLobby;
import dev.rodrigo.slashlobby.bungee.LobbyBungee;
import dev.rodrigo.slashlobby.util.ConfigContainer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BungeeLobbyCommand extends Command {

    static final String RELOAD_PERMISSION = "slashlobby.reload";

    public BungeeLobbyCommand() {
        super(
                "lobby",
                null,
                ConfigContainer.COMMAND_ALIASES.toArray(String[]::new)
        );
    }

    @Override
    public void execute(CommandSender source, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            // Check if the sender has the correct permission
            if (source.hasPermission(RELOAD_PERMISSION)) {
                // Reload the config
                ConfigContainer.init(SlashLobby.instance);
                source.sendMessage(
                        TextComponent.fromLegacyText(
                                LobbyBungee.replacePlaceholders(
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
                        TextComponent.fromLegacyText(
                                LobbyBungee.replacePlaceholders(
                                        ConfigContainer.NO_PERMISSION_MESSAGE,
                                        source
                                )
                        )
                );
                return;
            }
        }

        // Check if the sender is a player
        if (source instanceof ProxiedPlayer) {
            // Create a connection request to the lobby
            LobbyBungee.createConnectionRequest((ProxiedPlayer) source);
            return;
        }

        // Send the error message if the sender is not a player
        source.sendMessage(
                TextComponent.fromLegacyText(
                        LobbyBungee.replacePlaceholders(
                                ConfigContainer.ERROR_CONSOLE_MESSAGE,
                                source
                        )
                )
        );
    }
}
