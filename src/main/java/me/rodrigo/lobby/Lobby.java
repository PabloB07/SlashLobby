package me.rodrigo.lobby;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.md_5.bungee.api.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "lobby",
        name = "Lobby",
        version = BuildConstants.VERSION,
        authors = {"Rodrigo R."},
        description = "/lobby for BungeeCord/Waterfall/Velocity"
)
public class Lobby {

    @Inject
    private Logger logger;
    private ProxyServer proxy;
    private Path dataDirectory;
    private RegisteredServer registeredServer;

    @Inject
    public Lobby(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
