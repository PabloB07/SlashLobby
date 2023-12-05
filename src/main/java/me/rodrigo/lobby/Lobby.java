package me.rodrigo.lobby;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.rodrigo.lobby.command.velocity.LobbyVelocity;
import me.rodrigo.lobby.lib.Http;
import me.rodrigo.lobby.lib.Parser;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(
        id = "lobby",
        name = "Lobby",
        version = BuildConstants.VERSION,
        authors = {"Rodrigo R."},
        description = "/lobby for BungeeCord/Waterfall/Velocity"
)
public class Lobby {

    @Inject
    private final Logger logger;
    private final ProxyServer proxy;
    private Parser config;

    @Inject
    public Lobby(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        if (!dataDirectory.toFile().exists() && ! dataDirectory.toFile().mkdirs()) {
            logger.error("Could not create data directory");
            return;
        }

        try {
            if (!dataDirectory.resolve("config.yml").toFile().exists()) {
                Http.DownloadConfig(dataDirectory.toString() + "/config.yml");
            }
            config = new Parser(dataDirectory.resolve("config.yml"));
        } catch (IOException e) {
            logger.error("Could not download/read the config file. Error: " + e.getMessage());
            return;
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        final Optional<RegisteredServer> lobby = proxy.getServer(config.AsString("server"));
        if (lobby.isEmpty()) {
            logger.error("Could not find server: " + config.AsString("server"));
            return;
        }

        RegisteredServer registeredServer = lobby.get();
        proxy.getCommandManager().register("lobby", new LobbyVelocity(registeredServer, config));
        for(final String alias : config.AsStringList("aliases")) {
            proxy.getCommandManager().register(alias, new LobbyVelocity(registeredServer, config));
        }
        logger.info("Successfully enabled");
    }
}
