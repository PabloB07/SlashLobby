package dev.rodrigo.slashlobby.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.rodrigo.slashlobby.SlashLobby;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.FileNotFoundException;

public class SlashVelocity implements SimpleCommand {
    private final SlashLobby plugin;
    public SlashVelocity(SlashLobby plugin) {
        this.plugin = plugin;
    }
    @Override
    public void execute(Invocation invocation) {
        plugin.proxyServer.getScheduler().buildTask(plugin, () -> {
            if (!(invocation.source() instanceof ProxiedPlayer) && invocation.arguments().length > 0 && invocation.arguments()[0].equalsIgnoreCase("reload")) {
                try {
                    plugin.ReloadConfig();
                    invocation.source().sendMessage(
                            Component.text(plugin.config.AsString("messages.config_reload")
                                    .replaceAll("&", "§"))
                    );
                    return;
                } catch (FileNotFoundException ignored) {}
            }
            if (!(invocation.source() instanceof Player)) {
                invocation.source().sendMessage(
                        Component.text(plugin.config.AsString("messages.error_console").replaceAll("&", "§"))
                );
                return;
            }
            final String[] args = invocation.arguments();
            final Player player = (Player) invocation.source();
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                try {
                    if (player.hasPermission("slashlobby.reload")) {
                        plugin.ReloadConfig();
                        player.sendMessage(
                                Component.text(plugin.config.AsString("messages.config_reload")
                                                .replaceAll("(?i)\\{player}", player.getUsername())
                                        .replaceAll("&", "§"))
                        );
                    } else {
                        if (plugin.config.AsBoolean("forward_no_permission")) {
                            plugin.CreateConnectionRequest(player);
                            return;
                        }
                        player.sendMessage(
                                Component.text(plugin.config.AsString("messages.no_permission")
                                                .replaceAll("(?i)\\{player}", player.getUsername())
                                        .replaceAll("&", "§"))
                        );
                        return;
                    }
                } catch (FileNotFoundException ignored) {}
            }
            plugin.CreateConnectionRequest(player);
        }).schedule();
    }
}
