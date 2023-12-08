package me.rodrigo.slashlobby;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.rodrigo.slashlobby.command.velocity.LobbyVelocity;
import me.rodrigo.slashlobby.lib.Parser;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Plugin(
        id = "slashlobby",
        name = "SlashLobby",
        version = "1.3",
        description = "/lobby for BungeeCord/Waterfall/Velocity",
        authors = {"Rodrigo R."}
)
public class SlashLobby {
    @Inject
    private final Logger logger;
    private final ProxyServer proxy;
    private Parser config;

    @Inject
    public SlashLobby(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        if (!dataDirectory.toFile().exists() && ! dataDirectory.toFile().mkdirs()) {
            logger.error("Could not create data directory");
            return;
        }

        try {
            if (!dataDirectory.resolve("config.yml").toFile().exists()) {
                final InputStream stream = getClass().getClassLoader().getResourceAsStream("config.yml");
                if (stream == null) {
                    logger.error("Could not get the config file");
                    return;
                }
                Files.copy(stream, dataDirectory.resolve("config.yml"), StandardCopyOption.REPLACE_EXISTING);
            }
            config = new Parser(dataDirectory.resolve("config.yml"));
        } catch (IOException e) {
            logger.error("Could not get/read the config file. Error: " + e.getMessage());
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
