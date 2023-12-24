package dev.rodrigo.slashlobby.bungee;
import dev.rodrigo.slashlobby.commands.SlashBungee;
import dev.rodrigo.slashlobby.lib.Parser;
import dev.rodrigo.slashlobby.lib.TimerCache;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class LobbyBungee extends Plugin implements Listener {
    public Parser config;
    public ProxyServer proxyServer;
    private ServerInfo server;
    private Path dataDir;
    private final List<TimerCache> timerCaches = new ArrayList<>();

    public void ReloadConfig() throws FileNotFoundException {
        config = new Parser(dataDir.resolve("config.yml"));
        timerCaches.forEach(a -> a.setParser(config));
    }

    private void SendTitle(ProxiedPlayer player, String child) {
        if (config.AsBoolean("titles.enabled")) {
            player.sendTitle(
                    getProxy().createTitle()
                            .title(
                                    TextComponent.fromLegacyText(
                                            config.AsString("titles." + child + ".title").replaceAll("(?i)\\{player}", player.getName()).replaceAll("&", "§")
                                    )
                            )
                            .subTitle(
                                    TextComponent.fromLegacyText(
                                            config.AsString("titles." + child + ".subtitle").replaceAll("(?i)\\{player}", player.getName()).replaceAll("&", "§")
                                    )
                            )
                            .stay(config.AsInt("titles.stay"))
                            .fadeIn(config.AsInt("titles.fadein"))
                            .fadeOut(config.AsInt("titles.fadeout"))
            );
        }
    }

    public void CreateConnectionRequest(ProxiedPlayer player) {
        final Server currentServer = player.getServer();
        if (currentServer == null) {
            SendTitle(player, "internal_error");
            player.sendMessage(
                    TextComponent.fromLegacyText(config.AsString("messages.internal_error")
                            .replaceAll("(?i)\\{player}", player.getName())
                            .replaceAll("&", "§"))
            );
            return;
        }
        if (currentServer.getInfo().getName().equals(server.getName())) {
            SendTitle(player, "already_in_lobby");
            player.sendMessage(
                    TextComponent.fromLegacyText(config.AsString("messages.already_in_lobby")
                            .replaceAll("(?i)\\{player}", player.getName())
                            .replaceAll("&", "§"))
            );
            return;
        }
        if ( config.AsStringList("disabled_servers").stream().anyMatch(a -> a.equals(currentServer.getInfo().getName())) ) {
            SendTitle(player, "disabled");
            player.sendMessage(
                    TextComponent.fromLegacyText(config.AsString("messages.disabled")
                            .replaceAll("(?i)\\{player}", player.getName())
                            .replaceAll("&", "§"))
            );
            return;
        }
        if (config.AsBoolean("cooldown.enabled")) {
            final Optional<TimerCache> cache = timerCaches.stream().filter(a -> a.getUuid().equals(
                    player.getUniqueId()
            )).findFirst();
            if (cache.isPresent()) {
                if (!cache.get().canUse()) {
                    SendTitle(player, "cooldown");
                    player.sendMessage(
                            TextComponent.fromLegacyText(config.AsString("messages.cooldown")
                                    .replaceAll("(?i)\\{time}", String.valueOf(
                                            cache.get().getCurrentCooldown()
                                    ))
                                    .replaceAll("(?i)\\{player}", player.getName())
                                    .replaceAll("&", "§"))
                    );
                    return;
                }
                cache.get().registerNewUsage();
            } else {
                timerCaches.add(new TimerCache()
                        .setUuid(player.getUniqueId())
                        .setLastUsed(System.currentTimeMillis())
                        .setParser(config)
                        .registerNewUsage()
                );
            }
        }
        SendTitle(player, "sending");
        player.sendMessage(
                TextComponent.fromLegacyText(config.AsString("messages.sending")
                        .replaceAll("(?i)\\{player}", player.getName())
                        .replaceAll("&", "§"))
        );
        player.connect(server);
    }

    @Override
    public void onEnable() {
        this.proxyServer = getProxy();
        Logger logger = getLogger();
        this.dataDir = getDataFolder().toPath();
        if (!dataDir.toFile().exists() && !dataDir.toFile().mkdirs()) {
            logger.severe("Failed to create data directory");
            return;
        }
        try {
            if (!dataDir.resolve("config.yml").toFile().exists()) {
                logger.info("Config file not found, creating it...");
                final InputStream configStream = getClass().getClassLoader().getResourceAsStream("config.yml");
                if (configStream == null) {
                    logger.severe("Config file not found");
                    return;
                }
                Files.copy(configStream, dataDir.resolve("config.yml"));
            }
            config = new Parser(dataDir.resolve("config.yml"));
            logger.info("Config loaded");
        } catch (Exception e) {
            logger.severe("Failed to load config due to: "+ e);
        }
        final ServerInfo registeredServer = proxyServer.getServerInfo(config.AsString("server"));
        if (registeredServer == null) {
            logger.severe("Server not found");
            return;
        }
        server = registeredServer;
        logger.info("Server loaded");
        proxyServer.getPluginManager().registerCommand(this, new SlashBungee(this, "lobby"));
        for (String a : config.AsStringList("aliases")) {
            proxyServer.getPluginManager().registerCommand(this, new SlashBungee(this, a));
        }
        proxyServer.getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        timerCaches.removeIf(a -> a.getUuid().toString().equals(e.getPlayer().getUniqueId().toString()));
    }
}
