package me.rodrigo.slashlobby.bungee;

import me.rodrigo.slashlobby.command.bungee.LobbyBungee;
import me.rodrigo.slashlobby.lib.Parser;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
                final InputStream stream = getResourceAsStream("config.yml");
                if (stream == null) {
                    logger.severe("Could not get the config file");
                    return;
                }
                Files.copy(stream, dataDirectory.resolve("config.yml"), StandardCopyOption.REPLACE_EXISTING);
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
