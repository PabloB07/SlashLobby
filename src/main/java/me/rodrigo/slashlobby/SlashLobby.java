package me.rodrigo.slashlobby;

import com.google.inject.Inject;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.rodrigo.slashlobby.command.velocity.LobbyVelocity;
import me.rodrigo.slashlobby.lib.MinecraftColorCode;
import me.rodrigo.slashlobby.lib.Parser;
import me.rodrigo.slashlobby.lib.TimerCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Plugin(
        id = "slashlobby",
        name = "SlashLobby",
        version = "1.8",
        description = "/lobby for BungeeCord/Waterfall/Velocity",
        authors = {"Rodrigo R."}
)
public class SlashLobby {
    @Inject
    private final Logger logger;
    private final ProxyServer proxy;
    private Parser config;
    private final List<TimerCache> timers = new ArrayList<>();
    private final Path dataDirectory;
    private RegisteredServer lobby;

    public Parser ReloadConfig() throws FileNotFoundException {
        config = new Parser(dataDirectory.resolve("config.yml"));
        return config;
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent e) {
        timers.removeIf(a -> a.getUuid().toString().equals(e.getPlayer().getUniqueId().toString()));
    }

    public void SendErrorMessage(String j) {
        logger.error(j);
    }

    public void SendTitle(Player plr, String configProp, int canUseAgain) {
        if (config.AsBoolean("titles.enabled")) {
            plr.showTitle(
                    Title.title(
                            Component.text(
                                    MinecraftColorCode.ReplaceAllAmpersands(config.AsString("titles." + configProp + ".title")
                                            .replaceAll("(?i)\\{time\\}",
                                                    String.valueOf(canUseAgain)
                                            ).replaceAll("(?i)\\{player\\}", plr.getUsername()))
                            ),
                            Component.text(
                                    MinecraftColorCode.ReplaceAllAmpersands(config.AsString("titles." + configProp + ".subtitle")
                                            .replaceAll("(?i)\\{time\\}",
                                                    String.valueOf(canUseAgain)
                                            ).replaceAll("(?i)\\{player\\}", plr.getUsername()))
                            ),
                            Title.Times.times(
                                    Duration.ofSeconds(Long.parseLong(config.AsObject("titles.fadein").toString())),
                                    Duration.ofSeconds(Long.parseLong(config.AsObject("titles.stay").toString())),
                                    Duration.ofSeconds(Long.parseLong(config.AsObject("titles.fadeout").toString()))
                            )
                    )
            );
        }
    }

    public void CreateConnectionRequest(Player plr) {
        proxy.getScheduler().buildTask(this, () -> {
            if (lobby != null) {
                if (config.AsBoolean("cooldown.enabled")) {
                    final Optional<TimerCache> cache = timers.stream().filter(a -> a.getUuid().toString().equals(plr.getUniqueId().toString())).findFirst();
                    if (cache.isPresent()) {
                        if (!cache.get().canUse()) {
                            final int canUseAgain = cache.get().getCurrentCooldown();
                            final String message = config.AsString("messages.cooldown")
                                    .replaceAll("(?i)\\{time\\}",
                                            String.valueOf(canUseAgain)
                                    ).replaceAll("(?i)\\{player\\}", plr.getUsername());
                            if (!message.trim().isEmpty()) {
                                plr.sendMessage(Component.text(
                                        MinecraftColorCode.ReplaceAllAmpersands(message)
                                ));
                            }
                            if (config.AsBoolean("titles.enabled")) {
                                SendTitle(plr, "cooldown", canUseAgain);
                            }
                            return;
                        }
                        cache.get().registerNewUsage();
                        plr.createConnectionRequest(lobby).connect();
                        if (config.AsBoolean("titles.enabled")) {
                            SendTitle(plr, "sending", 0);
                        }
                        {
                            final String message = config.AsString("messages.sending");
                            if (!message.trim().isEmpty()) {
                                plr.sendMessage(Component.text(
                                        MinecraftColorCode.ReplaceAllAmpersands(message)
                                ));
                            }
                        }
                        return;
                    }
                    plr.createConnectionRequest(lobby).connect();
                    if (config.AsBoolean("titles.enabled")) {
                        SendTitle(plr, "sending", 0);
                    }
                    {
                        final String message = config.AsString("messages.sending");
                        if (!message.trim().isEmpty()) {
                            plr.sendMessage(Component.text(
                                    MinecraftColorCode.ReplaceAllAmpersands(message)
                            ));
                        }
                    }
                    timers.add(new TimerCache()
                            .setParser(config)
                            .setUuid(plr.getUniqueId())
                            .registerNewUsage());
                    return;
                }
                if (config.AsBoolean("titles.enabled")) {
                    SendTitle(plr, "sending", 0);
                }
                {
                    final String message = config.AsString("messages.sending");
                    if (!message.trim().isEmpty()) {
                        plr.sendMessage(Component.text(
                                MinecraftColorCode.ReplaceAllAmpersands(message)
                        ));
                    }
                }
                plr.createConnectionRequest(lobby).connect();
            }
        }).schedule();
    }

    @Inject
    public SlashLobby(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
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

        this.lobby = lobby.get();
        RegisteredServer registeredServer = lobby.get();
        proxy.getCommandManager().register("lobby", new LobbyVelocity(registeredServer, config, this));
        for(final String alias : config.AsStringList("aliases")) {
            proxy.getCommandManager().register(alias, new LobbyVelocity(registeredServer, config, this));
        }
        logger.info("Successfully enabled");
    }
}
