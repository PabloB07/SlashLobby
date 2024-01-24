package dev.rodrigo.slashlobby.bungee;

import dev.rodrigo.slashlobby.command.BungeeLobbyCommand;
import dev.rodrigo.slashlobby.util.ConfigContainer;
import dev.rodrigo.slashlobby.util.CoolDownCacheStorage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class LobbyBungee extends Plugin {
    Logger logger;
    public ProxyServer proxyServer;
    public Path dataDir;

    static Configuration configurationNode;
    static ServerInfo LOBBY_SERVER;
    static TaskScheduler SCHEDULER;

    // Load titles from config
    public static Title SENDING_TITLE;
    public static Title ERROR_COOL_DOWN_TITLE;
    public static Title ALREADY_IN_LOBBY_TITLE;
    public static Title DISABLED_TITLE;
    public static Title INTERNAL_ERROR_TITLE;

    // Define the instance so we can reload
    public static LobbyBungee instance;

    // BungeeCors uses the TextComponent API, instead of Velocity's Adventure API
    // public static MiniMessage messageColor;

    @Override
    public void onEnable() {
        // On startup
        this.proxyServer = getProxy();
        this.logger = getLogger();
        this.dataDir = getDataFolder().toPath();
        instance = this;

        // Code rebuilt on v.2.2
        // Data folder may not exist yet on first run
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

            // Use the built-in configuration loader
            // Using external loaders is possible, however Velocity has its own loader
            // As of 1.0 to 2.1, the plugin was using an own loader, which is deprecated
            // This was fixed in 2.2
            configurationNode = YamlConfiguration.getProvider(YamlConfiguration.class)
                    .load(dataDir.resolve("config.yml").toFile());

            // Load all the config into variables
            ConfigContainer.init(this);

            SENDING_TITLE = proxyServer.createTitle()
                    .title(
                            TextComponent.fromLegacyText(
                                    ConfigContainer.TITLES_SENDING_TITLE
                            )
                    ).title(
                            TextComponent.fromLegacyText(
                                    ConfigContainer.TITLES_SENDING_SUBTITLE
                            )
                    )
                    .fadeIn(ConfigContainer.TITLES_FADE_IN)
                    .fadeOut(ConfigContainer.TITLES_FADEOUT)
                    .stay(ConfigContainer.TITLES_STAY);

            ERROR_COOL_DOWN_TITLE = proxyServer.createTitle()
                    .title(
                            TextComponent.fromLegacyText(
                                    ConfigContainer.TITLES_COOL_DOWN_TITLE
                            )
                    ).title(
                            TextComponent.fromLegacyText(
                                    ConfigContainer.TITLES_COOL_DOWN_SUBTITLE
                            )
                    )
                    .fadeIn(ConfigContainer.TITLES_FADE_IN)
                    .fadeOut(ConfigContainer.TITLES_FADEOUT)
                    .stay(ConfigContainer.TITLES_STAY);

            ALREADY_IN_LOBBY_TITLE = proxyServer.createTitle()
                    .title(
                            TextComponent.fromLegacyText(
                                    ConfigContainer.TITLES_ALREADY_IN_LOBBY_TITLE
                            )
                    ).title(
                            TextComponent.fromLegacyText(
                                    ConfigContainer.TITLES_ALREADY_IN_LOBBY_SUBTITLE
                            )
                    )
                    .fadeIn(ConfigContainer.TITLES_FADE_IN)
                    .fadeOut(ConfigContainer.TITLES_FADEOUT)
                    .stay(ConfigContainer.TITLES_STAY);

            DISABLED_TITLE = proxyServer.createTitle()
                    .title(
                            TextComponent.fromLegacyText(
                                    ConfigContainer.TITLES_DISABLED_TITLE
                            )
                    ).title(
                            TextComponent.fromLegacyText(
                                    ConfigContainer.TITLES_DISABLED_SUBTITLE
                            )
                    )
                    .fadeIn(ConfigContainer.TITLES_FADE_IN)
                    .fadeOut(ConfigContainer.TITLES_FADEOUT)
                    .stay(ConfigContainer.TITLES_STAY);

            INTERNAL_ERROR_TITLE = proxyServer.createTitle()
                    .title(
                            TextComponent.fromLegacyText(
                                    ConfigContainer.TITLES_INTERNAL_ERROR_TITLE
                            )
                    ).title(
                            TextComponent.fromLegacyText(
                                    ConfigContainer.TITLES_INTERNAL_ERROR_SUBTITLE
                            )
                    )
                    .fadeIn(ConfigContainer.TITLES_FADE_IN)
                    .fadeOut(ConfigContainer.TITLES_FADEOUT)
                    .stay(ConfigContainer.TITLES_STAY);

            // Init the cool down cache if enabled
            if (ConfigContainer.COOL_DOWN_ENABLED) CoolDownCacheStorage.init();

            // Load the server
            final String server = ConfigContainer.LOBBY_SERVER;
            final ServerInfo registeredServer = proxyServer.getServerInfo(server);
            if (registeredServer == null) {
                logger.severe("Lobby server not found! Please check your config (server: "+ server +"). The plugin will enable, but it won't work.");
                return;
            }
            LOBBY_SERVER = registeredServer;
            SCHEDULER = proxyServer.getScheduler();

            logger.info("Config loaded");

            // Register all commands with their aliases
            proxyServer.getPluginManager().registerCommand(
                    this,
                    new BungeeLobbyCommand()
            );
        } catch (Exception e) {
            logger.severe("Failed to load config due to: "+ e);
        }
    }

    // This method is used to forward a player to the lobby
    public static void createConnectionRequest(@Nonnull ProxiedPlayer player) {
        final Server server = player.getServer();
        // If for any reason the player is not connected to a server, don't do anything
        if (server == null) {
            sendTitle(player, INTERNAL_ERROR_TITLE);
            player.sendMessage(
                    TextComponent.fromLegacyText(
                            replacePlaceholders(
                                    ConfigContainer.INTERNAL_ERROR_MESSAGE,
                                    player
                            )
                    )
            );
            return;
        }

        final ServerInfo registeredServer = server.getInfo();

        // If the player is already in the lobby, don't do anything
        if (registeredServer == LOBBY_SERVER) {
            sendTitle(player, ALREADY_IN_LOBBY_TITLE);
            player.sendMessage(
                    TextComponent.fromLegacyText(
                            replacePlaceholders(ConfigContainer.ALREADY_IN_LOBBY_MESSAGE, player)
                    )
            );
            return;
        }

        // Check for any cool down
        if (ConfigContainer.COOL_DOWN_ENABLED) {
            final long timeElapsedSinceLastUsage = CoolDownCacheStorage.getTimeElapsedSinceLastUsage(player.getUniqueId());
            final long realTimeElapsed = System.currentTimeMillis() - timeElapsedSinceLastUsage;
            if (timeElapsedSinceLastUsage > -1 &&
                    // Logic: if the time elapsed since the last usage is less than the cool down registered time, the player can't use the command again
                    realTimeElapsed < CoolDownCacheStorage.getCoolDownRegisteredTime()
            ) {
                // Divide by 1000 to get seconds instead of milliseconds
                final long missingTime = (CoolDownCacheStorage.getCoolDownRegisteredTime() - realTimeElapsed) / 1000;
                // -1 means that no usage was found
                sendTitle(player, ERROR_COOL_DOWN_TITLE);
                player.sendMessage(
                        TextComponent.fromLegacyText(
                                replacePlaceholders(
                                        ConfigContainer.ERROR_COOL_DOWN_MESSAGE
                                                .replaceAll("\\{time}", String.valueOf(missingTime)),
                                        player
                                )
                        )
                );
                return;
            }
        }

        // Check if the player is on a forbidden server
        if (ConfigContainer.DISABLED_SERVERS.contains(registeredServer.getName())) {
            sendTitle(player, DISABLED_TITLE);
            player.sendMessage(
                    TextComponent.fromLegacyText(
                            replacePlaceholders(
                                    ConfigContainer.DISABLED_SERVER_MESSAGE,
                                    player
                            )
                    )
            );
            return;
        }

        sendTitle(player, ERROR_COOL_DOWN_TITLE);
        player.sendMessage(
                TextComponent.fromLegacyText(
                        replacePlaceholders(
                                ConfigContainer.ERROR_COOL_DOWN_MESSAGE,
                                player
                        )
                )
        );

        CoolDownCacheStorage.registerUsage(player.getUniqueId());

        // Forward the player into the lobby
        doDelay((Void ignored) -> player.connect(LOBBY_SERVER));
    }

    public static void doDelay(Consumer<Void> consumer) {
        // Execute the delay if enabled
        if (!ConfigContainer.DELAY_COMMANDS) consumer.accept(null);
        // Schedule the task
        SCHEDULER.schedule(
                LobbyBungee.instance,
                () -> consumer.accept(null),
                ConfigContainer.DELAY_COMMANDS_VALUE,
                ConfigContainer.DELAY_COMMANDS_UNIT
        );
    }

    public static void sendTitle(ProxiedPlayer player, Title title) {
        // Don't do anything if titles are disabled
        if (!ConfigContainer.TITLES_ENABLED) return;
        player.sendTitle(title);
    }

    // Create a getter
    // Refer To: util/ConfigContainer to see how this is implemented
    public Configuration getConfig() {
        return configurationNode;
    }

    public static String replacePlaceholders(String message, CommandSender player) {
        if (!(player instanceof ProxiedPlayer)) return message;
        return message
                .replaceAll("(?i)\\{player}", player.getName());
    }
}
