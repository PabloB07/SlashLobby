package dev.rodrigo.slashlobby.command;

import dev.rodrigo.slashlobby.bungee.LobbyBungee;
import dev.rodrigo.slashlobby.util.BungeeConfigLoader;
import dev.rodrigo.slashlobby.util.ConfigContainer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class ReloadBungeeCommand extends Command {

    public ReloadBungeeCommand() {
        super(
                "slobby",
                BungeeLobbyCommand.RELOAD_PERMISSION
        );
    }

    @Override
    public void execute(CommandSender source, String[] ignored) {
        // Check if the sender has the correct permission
        if (source.hasPermission(BungeeLobbyCommand.RELOAD_PERMISSION)) {
            // Reload the config
            try {
                LobbyBungee.instance.reInitConfig();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            BungeeConfigLoader.init(LobbyBungee.instance.getConfig());
            source.sendMessage(
                    TextComponent.fromLegacyText(
                            LobbyBungee.replacePlaceholders(
                                    ConfigContainer.CONFIG_RELOAD_MESSAGE,
                                    source
                            )
                    )
            );
        } else {
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
        }
    }
}
