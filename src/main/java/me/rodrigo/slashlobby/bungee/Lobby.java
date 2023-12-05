package me.rodrigo.slashlobby.bungee;

import me.rodrigo.slashlobby.command.bungee.LobbyBungee;
import me.rodrigo.slashlobby.lib.Http;
import me.rodrigo.slashlobby.lib.Parser;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class Lobby extends Plugin {
    @Override
    public void onEnable() {
        final Logger logger = getLogger();
        final Path dataDirectory = getDataFolder().toPath();
        final Parser config;
        if (!dataDirectory.toFile().exists() && !dataDirectory.toFile().mkdirs()) {
            logger.severe("Could not create data directory");
            return;
        }

        try {
            if (!dataDirectory.resolve("config.yml").toFile().exists()) {
                Http.DownloadConfig(dataDirectory.toString() + "/config.yml");
            }
            config = new Parser(dataDirectory.resolve("config.yml"));
        } catch (IOException e) {
            logger.severe("Could not download/read the config file. Error: " + e.getMessage());
            return;
        }
        final ServerInfo lobby = getProxy().getServerInfo(config.AsString("server"));
        if (lobby == null) {
            logger.severe("Could not find server: " + config.AsString("server"));
            return;
        }

        getProxy().getPluginManager().registerCommand(this, new LobbyBungee(lobby, config, "lobby"));
        for(final String alias : config.AsStringList("aliases")) {
            getProxy().getPluginManager().registerCommand(this, new LobbyBungee(lobby, config, alias));
        }
        logger.info("Successfully enabled");
    }
}
