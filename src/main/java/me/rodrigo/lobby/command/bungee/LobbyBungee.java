package me.rodrigo.lobby.command.bungee;

import me.rodrigo.lobby.lib.MinecraftColorCode;
import me.rodrigo.lobby.lib.Parser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;

import java.util.Optional;

public class LobbyBungee extends Command {
    private final ServerInfo server;
    private final Parser parser;

    public LobbyBungee(ServerInfo server, Parser parser, String name) {
        super(name);
        this.server = server;
        this.parser = parser;
    }

    @Override
    public void execute(CommandSender source, String[] args) {
        if (!(source instanceof ProxiedPlayer)) {
            source.sendMessage(TextComponent.fromLegacyText(
                    MinecraftColorCode.ReplaceAllAmpersands(parser.AsString("messages.error_console"))
            ));
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer) source;
        final Server serverConnection = player.getServer();
        if (serverConnection == null) {
            source.sendMessage(TextComponent.fromLegacyText(
                    MinecraftColorCode.ReplaceAllAmpersands(parser.AsString("messages.internal_error"))
            ));
            return;
        }
        if (serverConnection.getInfo().getName().equals(server.getName())) {
            source.sendMessage(TextComponent.fromLegacyText(
                    MinecraftColorCode.ReplaceAllAmpersands(parser.AsString("messages.already_in_lobby"))
            ));
            return;
        }

        player.connect(server);
        source.sendMessage(TextComponent.fromLegacyText(
                MinecraftColorCode.ReplaceAllAmpersands(parser.AsString("messages.sending"))
        ));
    }
}
