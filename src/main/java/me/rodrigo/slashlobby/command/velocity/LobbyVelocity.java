package me.rodrigo.slashlobby.command.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.rodrigo.slashlobby.lib.MinecraftColorCode;
import me.rodrigo.slashlobby.lib.Parser;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class LobbyVelocity implements SimpleCommand {
    private final RegisteredServer server;
    private final Parser parser;

    public LobbyVelocity(RegisteredServer server, Parser parser) {
        this.server = server;
        this.parser = parser;
    }

    @Override
    public void execute(Invocation invocation) {
        final CommandSource source = invocation.source();
        if (!(source instanceof Player)) {
            source.sendMessage(Component.text(
                    MinecraftColorCode.ReplaceAllAmpersands(parser.AsString("messages.error_console"))
            ));
            return;
        }
        final Player player = (Player) source;
        final Optional<ServerConnection> serverConnection = player.getCurrentServer();
        if (serverConnection.isEmpty()) {
            source.sendMessage(Component.text(
                    MinecraftColorCode.ReplaceAllAmpersands(parser.AsString("messages.internal_error"))
            ));
            return;
        }

        if (serverConnection.get().getServerInfo().getName().equals(server.getServerInfo().getName())) {
            source.sendMessage(Component.text(
                    MinecraftColorCode.ReplaceAllAmpersands(parser.AsString("messages.already_in_lobby"))
            ));
            return;
        }

        player.createConnectionRequest(server).connect();
        source.sendMessage(Component.text(
                MinecraftColorCode.ReplaceAllAmpersands(parser.AsString("messages.sending"))
        ));
    }
}
