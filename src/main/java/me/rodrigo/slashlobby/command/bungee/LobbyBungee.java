package me.rodrigo.slashlobby.command.bungee;

import me.rodrigo.slashlobby.bungee.Lobby;
import me.rodrigo.slashlobby.lib.MinecraftColorCode;
import me.rodrigo.slashlobby.lib.Parser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Command;

public class LobbyBungee extends Command {
    private final ServerInfo server;
    private final Parser parser;
    private final Lobby lobbyBungee;

    public LobbyBungee(ServerInfo server, Parser parser, String name, Lobby lobbyBungee) {
        super(name);
        this.server = server;
        this.parser = parser;
        this.lobbyBungee = lobbyBungee;
    }

    @Override
    public void execute(CommandSender source, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!source.hasPermission("slashlobby.reload")) {
                if (parser.AsBoolean("forward_no_permission") && source instanceof ProxiedPlayer) {
                    lobbyBungee.CreateConnectionRequest((ProxiedPlayer) source);
                }
                final String message = parser.AsString("messages.no_permission");
                if (message.trim().isEmpty()) return;
                source.sendMessage(TextComponent.fromLegacyText(
                        MinecraftColorCode.ReplaceAllAmpersands(message)
                ));
                return;
            }
            lobbyBungee.LoadConfig();
            final String message = parser.AsString("messages.config_reload");
            if (message.trim().isEmpty()) return;
            source.sendMessage(TextComponent.fromLegacyText(
                    MinecraftColorCode.ReplaceAllAmpersands(message)
            ));
            return;
        }
        if (!(source instanceof ProxiedPlayer)) {
            final String message = parser.AsString("messages.error_console");
            if (message.trim().isEmpty()) return;
            source.sendMessage(TextComponent.fromLegacyText(
                    MinecraftColorCode.ReplaceAllAmpersands(message)
            ));
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer) source;
        final Server serverConnection = player.getServer();
        if (serverConnection == null) {
            lobbyBungee.SendTitle(player, "internal_error", 0);
            final String message = parser.AsString("messages.internal_error");
            if (message.trim().isEmpty()) return;
            source.sendMessage(TextComponent.fromLegacyText(
                    MinecraftColorCode.ReplaceAllAmpersands(message)
            ));
            return;
        }
        if (serverConnection.getInfo().getName().equals(server.getName())) {
            lobbyBungee.SendTitle(player, "already_in_lobby", 0);
            final String message = parser.AsString("messages.already_in_lobby");
            if (message.trim().isEmpty()) return;
            source.sendMessage(TextComponent.fromLegacyText(
                    MinecraftColorCode.ReplaceAllAmpersands(message)
            ));
            return;
        }

        if (parser.AsStringList("disabled_servers").stream().anyMatch(a -> a.equals(serverConnection.getInfo().getName()))) {
            lobbyBungee.SendTitle(player, "disabled", 0);
            final String message = parser.AsString("messages.disabled");
            if (message.trim().isEmpty()) return;
            source.sendMessage(TextComponent.fromLegacyText(
                    MinecraftColorCode.ReplaceAllAmpersands(message)
            ));
            return;
        }

        lobbyBungee.CreateConnectionRequest(player);
        lobbyBungee.SendTitle(player, "sending", 0);
        {
            final String message = parser.AsString("messages.sending");
            if (message.trim().isEmpty()) return;
            source.sendMessage(TextComponent.fromLegacyText(
                    MinecraftColorCode.ReplaceAllAmpersands(message)
            ));
        }
    }
}
