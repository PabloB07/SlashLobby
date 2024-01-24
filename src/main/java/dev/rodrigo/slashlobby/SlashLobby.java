package dev.rodrigo.slashlobby;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.Scheduler;
import dev.rodrigo.slashlobby.command.VelocityLobbyCommand;
import dev.rodrigo.slashlobby.command.VelocityReloadCommand;
import dev.rodrigo.slashlobby.util.ConfigContainer;
import dev.rodrigo.slashlobby.util.CoolDownCacheStorage;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Consumer;

@Plugin(
        id = "slashlobby",
        name = "SlashLobby",
        version = "2.2",
        authors = {"Rodrigo R."}
)
public class SlashLobby {
    final Logger logger;
    public ProxyServer proxyServer;
    public Path dataDir;

    static CommentedConfigurationNode configurationNode;
    static RegisteredServer LOBBY_SERVER;
    static Scheduler SCHEDULER;

    // Load titles from config
    public static Title SENDING_TITLE;
    public static Title ERROR_COOL_DOWN_TITLE;
    public static Title ALREADY_IN_LOBBY_TITLE;
    public static Title DISABLED_TITLE;
    public static Title INTERNAL_ERROR_TITLE;

    // Define the instance so we can reload
    public static SlashLobby instance;

    // Define MiniMessage for Velocity servers as it uses the adventure API
    // Instead of Bungee's TextComponent API
    public static MiniMessage messageColor;

    @Inject
    public SlashLobby(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDir) {
        // On startup
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDir = dataDir;
        instance = this;

        messageColor = MiniMessage.miniMessage();

        // Code rebuilt on v.2.2
        // Data folder may not exist yet on first run
        if (!dataDir.toFile().exists() && !dataDir.toFile().mkdirs()) {
            logger.error("Failed to create data directory");
            return;
        }
        try {
            if (!dataDir.resolve("config.yml").toFile().exists()) {
                logger.info("Config file not found, creating it...");
                // As said, Velocity uses the Adventure API
                // There's a default config for Velocity
                final InputStream configStream = getClass().getClassLoader().getResourceAsStream("config_velocity.yml");
                if (configStream == null) {
                    logger.error("Config file not found");
                    return;
                }
                Files.copy(configStream, dataDir.resolve("config.yml"));
            }

            // Use the built-in configuration loader
            // Using external loaders is possible, however Velocity has its own loader
            // As of 1.0 to 2.1, the plugin was using an own loader, which is deprecated
            // This was fixed in 2.2
            configurationNode = YamlConfigurationLoader.builder().path(
                    dataDir.resolve("config.yml")
            ).build().load();

            // Load all the config into variables
            ConfigContainer.init(this);

            // Load all the titles
            // Recycle the times once
            final Title.Times times = Title.Times.times(
                    Duration.of(
                            ConfigContainer.TITLES_FADE_IN,
                            ChronoUnit.SECONDS
                    ),
                    Duration.of(
                            ConfigContainer.TITLES_STAY,
                            ChronoUnit.SECONDS
                    ),
                    Duration.of(
                            ConfigContainer.TITLES_FADEOUT,
                            ChronoUnit.SECONDS
                    )
            );

            SENDING_TITLE = Title.title(
                    messageColor.deserialize(
                            ConfigContainer.TITLES_SENDING_TITLE
                    ),
                    messageColor.deserialize(
                            ConfigContainer.TITLES_SENDING_SUBTITLE
                    ),
                    times
            );

            ERROR_COOL_DOWN_TITLE = Title.title(
                    messageColor.deserialize(
                            ConfigContainer.TITLES_COOL_DOWN_TITLE
                    ),
                    messageColor.deserialize(
                            ConfigContainer.TITLES_COOL_DOWN_SUBTITLE
                    ),
                    times
            );

            ALREADY_IN_LOBBY_TITLE = Title.title(
                    messageColor.deserialize(
                            ConfigContainer.TITLES_ALREADY_IN_LOBBY_TITLE
                    ),
                    messageColor.deserialize(
                            ConfigContainer.TITLES_ALREADY_IN_LOBBY_SUBTITLE
                    ),
                    times
            );

            DISABLED_TITLE = Title.title(
                    messageColor.deserialize(
                            ConfigContainer.TITLES_DISABLED_TITLE
                    ),
                    messageColor.deserialize(
                            ConfigContainer.TITLES_DISABLED_SUBTITLE
                    ),
                    times
            );

            INTERNAL_ERROR_TITLE = Title.title(
                    messageColor.deserialize(
                            ConfigContainer.TITLES_INTERNAL_ERROR_TITLE
                    ),
                    messageColor.deserialize(
                            ConfigContainer.TITLES_INTERNAL_ERROR_SUBTITLE
                    ),
                    times
            );

            // Init the cool down cache if enabled
            if (ConfigContainer.COOL_DOWN_ENABLED) CoolDownCacheStorage.init();

            // Load the server
            final String server = ConfigContainer.LOBBY_SERVER;
            final Optional<RegisteredServer> registeredServer = proxyServer.getServer(server);
            if (registeredServer.isEmpty()) {
                logger.error("Lobby server not found! Please check your config (server: "+ server +"). The plugin will enable, but it won't work.");
                return;
            }
            LOBBY_SERVER = registeredServer.get();
            SCHEDULER = proxyServer.getScheduler();

            logger.info("Config loaded");

            // Register all commands with their aliases
            proxyServer.getCommandManager().register(
                    "lobby",
                    new VelocityLobbyCommand(),
                    ConfigContainer.COMMAND_ALIASES.toArray(String[]::new)
            );
            proxyServer.getCommandManager().register(
                    "slobby",
                    new VelocityReloadCommand()
            );
        } catch (Exception e) {
            logger.error("Failed to load config due to: "+ e);
        }
    }

    // This method is used to forward a player to the lobby
    public static void createConnectionRequest(@NotNull Player player) {
        final Optional<ServerConnection> server = player.getCurrentServer();
        // If for any reason the player is not connected to a server, don't do anything
        if (server.isEmpty()) {
            sendTitle(player, INTERNAL_ERROR_TITLE);
            player.sendMessage(
                    messageColor.deserialize(
                            replacePlaceholders(
                                    ConfigContainer.INTERNAL_ERROR_MESSAGE,
                                    player
                            )
                    )
            );
            return;
        }

        final RegisteredServer registeredServer = server.get().getServer();

        // If the player is already in the lobby, don't do anything
        if (registeredServer == LOBBY_SERVER) {
            sendTitle(player, ALREADY_IN_LOBBY_TITLE);
            player.sendMessage(
                    messageColor.deserialize(
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
                        messageColor.deserialize(
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
        if (ConfigContainer.DISABLED_SERVERS.contains(registeredServer.getServerInfo().getName())) {
            sendTitle(player, DISABLED_TITLE);
            player.sendMessage(
                    messageColor.deserialize(
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
                messageColor.deserialize(
                        replacePlaceholders(
                                ConfigContainer.ERROR_COOL_DOWN_MESSAGE,
                                player
                        )
                )
        );

        CoolDownCacheStorage.registerUsage(player.getUniqueId());

        // Forward the player into the lobby
        doDelay((Void ignored) -> player.createConnectionRequest(LOBBY_SERVER).connect());
    }

    public static void doDelay(Consumer<Void> consumer) {
        // Execute the delay if enabled
        if (!ConfigContainer.DELAY_COMMANDS) consumer.accept(null);
        // Schedule the task
        SCHEDULER.buildTask(
                SlashLobby.instance,
                () -> consumer.accept(null)
        ).delay(ConfigContainer.DELAY_COMMANDS_VALUE, ConfigContainer.DELAY_COMMANDS_UNIT);
    }

    public static void sendTitle(Player player, Title title) {
        // Don't do anything if titles are disabled
        if (!ConfigContainer.TITLES_ENABLED) return;
        player.showTitle(title);
    }

    // Create a getter
    // Refer To: util/ConfigContainer to see how this is implemented
    public CommentedConfigurationNode getConfig() {
        return configurationNode;
    }

    public static String replacePlaceholders(String message, CommandSource player) {
        if (!(player instanceof Player)) return message;
        return message
                .replaceAll("(?i)\\{player}", ((Player) player).getUsername());
    }
}
