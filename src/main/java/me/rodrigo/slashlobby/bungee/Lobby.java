package me.rodrigo.slashlobby.bungee;

import me.rodrigo.slashlobby.command.bungee.LobbyBungee;
import me.rodrigo.slashlobby.lib.MinecraftColorCode;
import me.rodrigo.slashlobby.lib.Parser;
import me.rodrigo.slashlobby.lib.TimerCache;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class Lobby extends Plugin implements Listener {
    private Parser config;
    private ServerInfo lobby;
    private final List<TimerCache> timers = new ArrayList<>();

    public void SendTitle(ProxiedPlayer plr, String configProp, int canUseAgain) {
        if (config.AsBoolean("titles.enabled")) {
            plr.sendTitle(
                    getProxy().createTitle()
                            .title(
                                    TextComponent.fromLegacyText(
                                            MinecraftColorCode.ReplaceAllAmpersands(config.AsString("titles." + configProp + ".title")
                                                    .replaceAll("(?i)\\{time\\}",
                                                            +canUseAgain + ""
                                                    ).replaceAll("(?i)\\{player\\}", plr.getName()))
                                    )
                            )
                            .subTitle(
                                    TextComponent.fromLegacyText(
                                            MinecraftColorCode.ReplaceAllAmpersands(config.AsString("titles." + configProp + ".subtitle")
                                                    .replaceAll("(?i)\\{time\\}",
                                                            +canUseAgain + ""
                                                    ).replaceAll("(?i)\\{player\\}", plr.getName()))
                                    )
                            )
                            .fadeIn(Integer.parseInt(config.AsObject("titles.fadein").toString()))
                            .stay(Integer.parseInt(config.AsObject("titles.stay").toString()))
                            .fadeOut(Integer.parseInt(config.AsObject("titles.fadeout").toString()))
            );
        }
    }

    public void CreateConnectionRequest(ProxiedPlayer plr) {
        getProxy().getScheduler().runAsync(this, () -> {
            if (lobby != null) {
                if (config.AsBoolean("cooldown.enabled")) {
                    final Optional<TimerCache> cache = timers.stream().filter(a -> a.getUuid().toString().equals(plr.getUniqueId().toString())).findFirst();
                    if (cache.isPresent()) {
                        if (!cache.get().canUse()) {
                            final int canUseAgain = cache.get().getCurrentCooldown();
                            final String message = config.AsString("messages.cooldown")
                                    .replaceAll("(?i)\\{time\\}",
                                            String.valueOf(canUseAgain)
                                    ).replaceAll("(?i)\\{player\\}", plr.getName());
                            if (!message.trim().isEmpty()) {
                                plr.sendMessage(TextComponent.fromLegacyText(
                                        MinecraftColorCode.ReplaceAllAmpersands(message)
                                ));
                            }
                            if (config.AsBoolean("titles.enabled")) {
                                SendTitle(plr, "cooldown", canUseAgain);
                            }
                            return;
                        }
                        cache.get().registerNewUsage();
                        plr.connect(lobby);
                        if (config.AsBoolean("titles.enabled")) {
                            SendTitle(plr, "sending", 0);
                        }
                        {
                            final String message = config.AsString("messages.sending");
                            if (!message.trim().isEmpty()) {
                                plr.sendMessage(TextComponent.fromLegacyText(
                                        MinecraftColorCode.ReplaceAllAmpersands(message)
                                ));
                            }
                        }
                        return;
                    }
                    plr.connect(lobby);
                    if (config.AsBoolean("titles.enabled")) {
                        SendTitle(plr, "sending", 0);
                    }
                    {
                        final String message = config.AsString("messages.sending");
                        if (!message.trim().isEmpty()) {
                            plr.sendMessage(TextComponent.fromLegacyText(
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
                        plr.sendMessage(TextComponent.fromLegacyText(
                                MinecraftColorCode.ReplaceAllAmpersands(message)
                        ));
                    }
                }
                plr.connect(lobby);
            }
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        timers.removeIf(a -> a.getUuid().toString().equals(e.getPlayer().getUniqueId().toString()));
    }

    public Parser LoadConfig() {
        final Logger logger = getLogger();
        final Path dataDirectory = getDataFolder().toPath();
        final Parser config;
        if (!dataDirectory.toFile().exists() && !dataDirectory.toFile().mkdirs()) {
            logger.severe("Could not create data directory");
            return null;
        }

        try {
            if (!dataDirectory.resolve("config.yml").toFile().exists()) {
                final InputStream stream = getResourceAsStream("config.yml");
                if (stream == null) {
                    logger.severe("Could not get the config file");
                    return null;
                }
                Files.copy(stream, dataDirectory.resolve("config.yml"), StandardCopyOption.REPLACE_EXISTING);
            }
            config = new Parser(dataDirectory.resolve("config.yml"));
        } catch (IOException e) {
            logger.severe("Could not download/read the config file. Error: " + e.getMessage());
            return null;
        }
        final ServerInfo lobby = getProxy().getServerInfo(config.AsString("server"));
        if (lobby == null) {
            logger.severe("Could not find server: " + config.AsString("server"));
            return null;
        }
        this.config = config;
        this.lobby = lobby;
        for ( final TimerCache cache : timers ) {
            cache.setParser(config);
        }
        return config;
    }

    @Override
    public void onEnable() {
        final Logger logger = getLogger();
        if (LoadConfig() == null) return;
        getProxy().getPluginManager().registerCommand(this, new LobbyBungee(lobby, config, "lobby", this));
        getProxy().getPluginManager().registerListener(this, this);
        for(final String alias : config.AsStringList("aliases")) {
            getProxy().getPluginManager().registerCommand(this, new LobbyBungee(lobby, config, alias, this));
        }
        logger.info("Successfully enabled");
    }
}
