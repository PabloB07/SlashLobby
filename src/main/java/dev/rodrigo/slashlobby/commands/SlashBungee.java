package dev.rodrigo.slashlobby.commands;

import dev.rodrigo.slashlobby.bungee.LobbyBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.FileNotFoundException;

public class SlashBungee extends Command {
    private final LobbyBungee plugin;
    public SlashBungee(LobbyBungee plugin, String name) {
        super(name);
        this.plugin = plugin;
    }
    @Override
    public void execute(CommandSender source, String[] args) {
        plugin.proxyServer.getScheduler().runAsync(plugin, () -> {
            if (!(source instanceof ProxiedPlayer) && args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                try {
                    plugin.ReloadConfig();
                    source.sendMessage(
                            TextComponent.fromLegacyText(plugin.config.AsString("messages.config_reload")
                                    .replaceAll("&", "§"))
                    );
                    return;
                } catch (FileNotFoundException ignored) {}
            }
            if (!(source instanceof ProxiedPlayer)) {
                source.sendMessage(
                        TextComponent.fromLegacyText(plugin.config.AsString("messages.error_console").replaceAll("&", "§"))
                );
                return;
            }
            final ProxiedPlayer player = (ProxiedPlayer) source;
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                try {
                    if (player.hasPermission("slashlobby.reload")) {
                        plugin.ReloadConfig();
                        player.sendMessage(
                                TextComponent.fromLegacyText(plugin.config.AsString("messages.config_reload")
                                        .replaceAll("(?i)\\{player}", player.getName())
                                        .replaceAll("&", "§"))
                        );
                    } else {
                        if (plugin.config.AsBoolean("forward_no_permission")) {
                            plugin.CreateConnectionRequest(player);
                            return;
                        }
                        player.sendMessage(
                                TextComponent.fromLegacyText(plugin.config.AsString("messages.no_permission")
                                        .replaceAll("(?i)\\{player}", player.getName())
                                        .replaceAll("&", "§"))
                        );
                        return;
                    }
                } catch (FileNotFoundException ignored) {}
            }
            plugin.CreateConnectionRequest(player);
        });
    }
}
