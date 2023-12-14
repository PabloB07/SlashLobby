package dev.rodrigo.slashlobby;

import com.google.inject.Inject;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.rodrigo.slashlobby.commands.SlashVelocity;
import dev.rodrigo.slashlobby.lib.Parser;
import dev.rodrigo.slashlobby.lib.TimerCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Plugin(
        id = "slashlobby",
        name = "SlashLobby",
        version = "1.9"
)
public class SlashLobby {
    private final Logger logger;
    public Parser config;
    public ProxyServer proxyServer;
    private RegisteredServer server;
    private final Path dataDir;
    private final List<TimerCache> timerCaches = new ArrayList<>();

    public void ReloadConfig() throws FileNotFoundException {
        config = new Parser(dataDir.resolve("config.yml"));
        timerCaches.forEach(a -> a.setParser(config));
    }

    private void SendTitle(Player player, String child) {
        if (config.AsBoolean("titles.enabled")) {
            player.showTitle(
                    Title.title(
                            Component.text(
                                    config.AsString("titles." + child + ".title").replaceAll("(?i)\\{player}", player.getUsername()).replaceAll("&", "§")
                            ),
                            Component.text(
                                    config.AsString("titles." + child + ".subtitle").replaceAll("(?i)\\{player}", player.getUsername()).replaceAll("&", "§")
                            ),
                            Title.Times.times(
                                    Duration.ofSeconds(config.AsInt("titles.fadein")),
                                    Duration.ofSeconds(config.AsInt("titles.stay")),
                                    Duration.ofSeconds(config.AsInt("titles.fadeout"))
                            )
                    )
            );
        }
    }

    public void CreateConnectionRequest(Player player) {
        final Optional<ServerConnection> a_currentServer = player.getCurrentServer();
        if (a_currentServer.isEmpty()) {
            SendTitle(player, "internal_error");
            player.sendMessage(
                    Component.text(config.AsString("messages.internal_error")
                            .replaceAll("(?i)\\{player}", player.getUsername())
                            .replaceAll("&", "§"))
            );
            return;
        }
        final ServerConnection currentServer = a_currentServer.get();
        if (currentServer.getServerInfo().getName().equals(server.getServerInfo().getName())) {
            SendTitle(player, "already_in_lobby");
            player.sendMessage(
                    Component.text(config.AsString("messages.already_in_lobby")
                            .replaceAll("(?i)\\{player}", player.getUsername())
                            .replaceAll("&", "§"))
            );
            return;
        }
        if ( config.AsStringList("disabled_servers").stream().anyMatch(a -> a.equals(currentServer.getServerInfo().getName())) ) {
            SendTitle(player, "disabled");
            player.sendMessage(
                    Component.text(config.AsString("messages.disabled")
                            .replaceAll("(?i)\\{player}", player.getUsername())
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
                            Component.text(config.AsString("messages.cooldown")
                                    .replaceAll("(?i)\\{time}", String.valueOf(
                                            cache.get().getCurrentCooldown()
                                    ))
                                            .replaceAll("(?i)\\{player}", player.getUsername())
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
                Component.text(config.AsString("messages.sending")
                        .replaceAll("(?i)\\{player}", player.getUsername())
                        .replaceAll("&", "§"))
        );
        player.createConnectionRequest(server).connect();
    }

    @Inject
    public SlashLobby(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDir) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDir = dataDir;
        if (!dataDir.toFile().exists() && !dataDir.toFile().mkdirs()) {
            logger.error("Failed to create data directory");
            return;
        }
        try {
            if (!dataDir.resolve("config.yml").toFile().exists()) {
                logger.info("Config file not found, creating it...");
                final InputStream configStream = getClass().getClassLoader().getResourceAsStream("config.yml");
                if (configStream == null) {
                    logger.error("Config file not found");
                    return;
                }
                Files.copy(configStream, dataDir.resolve("config.yml"));
            }
            config = new Parser(dataDir.resolve("config.yml"));
            logger.info("Config loaded");
        } catch (Exception e) {
            logger.error("Failed to load config due to: "+ e);
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        final Optional<RegisteredServer> registeredServer = proxyServer.getServer(config.AsString("server"));
        if (registeredServer.isEmpty()) {
            logger.error("Server not found");
            return;
        }
        server = registeredServer.get();
        logger.info("Server loaded");
        proxyServer.getCommandManager().register("lobby", new SlashVelocity(this));
        for (String a : config.AsStringList("aliases")) {
            proxyServer.getCommandManager().register(a, new SlashVelocity(this));
        }
    }

    @Subscribe
    private void onDisconnect(DisconnectEvent e) {
        timerCaches.removeIf(a -> a.getUuid().toString().equals(e.getPlayer().getUniqueId().toString()));
    }
}
