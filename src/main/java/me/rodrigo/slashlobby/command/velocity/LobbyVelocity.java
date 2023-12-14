package me.rodrigo.slashlobby.command.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.rodrigo.slashlobby.SlashLobby;
import me.rodrigo.slashlobby.lib.MinecraftColorCode;
import me.rodrigo.slashlobby.lib.Parser;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class LobbyVelocity implements SimpleCommand {
    private final RegisteredServer server;
    private Parser parser;
    private final SlashLobby slashLobby;

    public LobbyVelocity(RegisteredServer server, Parser parser, SlashLobby slashLobby) {
        this.server = server;
        this.parser = parser;
        this.slashLobby = slashLobby;
    }

    @Override
    public void execute(Invocation invocation) {
        final String[] args = invocation.arguments();
        final CommandSource source = invocation.source();
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!source.hasPermission("slashlobby.reload")) {
                if (parser.AsBoolean("forward_no_permission") && source instanceof Player) {
                    slashLobby.CreateConnectionRequest((Player) source);
                    return;
                }
                final String message = parser.AsString("messages.no_permission");
                if (message.trim().isEmpty()) return;
                source.sendMessage(Component.text(
                        MinecraftColorCode.ReplaceAllAmpersands(message)
                ));
                return;
            }
            try {
                parser = slashLobby.ReloadConfig();
                final String message = parser.AsString("messages.config_reload");
                if (message.trim().isEmpty()) return;
                source.sendMessage(Component.text(
                        MinecraftColorCode.ReplaceAllAmpersands(message)
                ));
            } catch (Exception e) {
                slashLobby.SendErrorMessage("The config file could not be found!");
            }
            return;
        }
        if (!(source instanceof Player)) {
            final String message = parser.AsString("messages.error_console");
            if (message.trim().isEmpty()) return;
            source.sendMessage(Component.text(
                    MinecraftColorCode.ReplaceAllAmpersands(message)
            ));
            return;
        }
        final Player player = (Player) source;
        final Optional<ServerConnection> serverConnection = player.getCurrentServer();
        if (serverConnection.isEmpty()) {
            slashLobby.SendTitle(player, "internal_error", 0);
            final String message = parser.AsString("messages.internal_error");
            if (message.trim().isEmpty()) return;
            source.sendMessage(Component.text(
                    MinecraftColorCode.ReplaceAllAmpersands(message)
            ));
            return;
        }
        if (serverConnection.get().getServerInfo().getName().equals(server.getServerInfo().getName())) {
            slashLobby.SendTitle(player, "already_in_lobby", 0);
            final String message = parser.AsString("messages.already_in_lobby");
            if (message.trim().isEmpty()) return;
            source.sendMessage(Component.text(
                    MinecraftColorCode.ReplaceAllAmpersands(message)
            ));
            return;
        }

        if (parser.AsStringList("disabled_servers").stream().anyMatch(a -> a.equals(serverConnection.get().getServerInfo().getName()))) {
            slashLobby.SendTitle(player, "disabled", 0);
            final String message = parser.AsString("messages.disabled");
            if (message.trim().isEmpty()) return;
            source.sendMessage(Component.text(
                    MinecraftColorCode.ReplaceAllAmpersands(message)
            ));
            return;
        }

        slashLobby.CreateConnectionRequest(player);
    }
}
