package net.dodian.uber.game.discord

import dev.kord.common.entity.Permission
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.embed
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.count
import net.dodian.uber.game.engine.config.discordChannelId
import net.dodian.uber.game.engine.config.discordGuildId
import net.dodian.uber.game.engine.config.discordStaffAlertChannelId
import net.dodian.uber.game.engine.config.discordToken
import net.dodian.uber.game.engine.systems.world.player.PlayerRegistry
import org.slf4j.LoggerFactory

data class DiscordRuntimeConfiguration(
    val token: String,
    val guildId: String,
    val announcementChannelId: String,
    val staffAlertChannelId: String,
) {
    fun enabled(): Boolean = token.isNotBlank()
    fun valid(): Boolean = enabled() && guildId.asSnowflakeOrNull() != null &&
        announcementChannelId.asSnowflakeOrNull() != null && staffAlertChannelId.asSnowflakeOrNull() != null
}

enum class DiscordAlertKind { PLAYER_LOGIN, PLAYER_LOGOUT, MODERATION, TRADE_SECURITY, LIFECYCLE }

object DiscordCommandPolicy {
    fun requiresAdministrator(command: String): Boolean = command in setOf("announce", "alert-test")
    fun isAllowed(command: String, administrator: Boolean): Boolean = !requiresAdministrator(command) || administrator
}

data class DiscordAlert(
    val kind: DiscordAlertKind,
    val title: String,
    val detail: String,
    val deduplicationKey: String,
)

private data class OutboundDiscordMessage(
    val channelId: Snowflake,
    val content: String,
    val deduplicationKey: String?,
    val embedTitle: String? = null,
)

/** Kord-only gateway adapter. Gameplay code publishes typed announcements and alerts through this boundary. */
object DiscordService {
    private const val MAX_ATTEMPTS = 3
    private const val DEDUPLICATION_WINDOW_MS = 30_000L
    private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    private val running = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val outbound = Channel<OutboundDiscordMessage>(capacity = 256, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val recentAlerts = java.util.concurrent.ConcurrentHashMap<String, Long>()

    @Volatile private var kord: Kord? = null
    @Volatile private var configuration = configuredRuntime()

    @JvmStatic
    fun start() = start(configuration)

    internal fun start(runtime: DiscordRuntimeConfiguration) {
        configuration = runtime
        if (!runtime.enabled()) {
            logger.info("Discord is disabled because DISCORD_TOKEN is blank.")
            return
        }
        if (!runtime.valid()) {
            logger.error("Discord is disabled: DISCORD_GUILD_ID, DISCORD_CHANNEL_ID, and DISCORD_STAFF_ALERT_CHANNEL_ID must be numeric snowflakes.")
            return
        }
        if (!running.compareAndSet(false, true)) return

        scope.launch {
            try {
                val client = Kord(runtime.token)
                kord = client
                installHandlers(client)
                registerCommands(client, Snowflake(runtime.guildId))
                launch { deliveryLoop(client) }
                client.login { presence { status = PresenceStatus.Online } }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                running.set(false)
                kord = null
                logger.error("Discord bot failed to start or disconnected.", failure)
            }
        }
    }

    @JvmStatic
    fun stop() {
        if (!running.getAndSet(false)) return
        val active = kord
        kord = null
        scope.launch {
            try {
                active?.shutdown()
            } catch (failure: Throwable) {
                logger.warn("Discord bot shutdown failed.", failure)
            }
        }
    }

    @JvmStatic
    fun sendToGeneral(message: String) {
        val channel = configuration.announcementChannelId.asSnowflakeOrNull() ?: return
        enqueue(OutboundDiscordMessage(channel, message.take(1_900), null))
    }

    @JvmStatic
    fun publishAlert(alert: DiscordAlert) {
        val channel = configuration.staffAlertChannelId.asSnowflakeOrNull() ?: return
        val now = System.currentTimeMillis()
        val previous = recentAlerts.put(alert.deduplicationKey, now)
        if (previous != null && now - previous < DEDUPLICATION_WINDOW_MS) return
        recentAlerts.entries.removeIf { now - it.value > DEDUPLICATION_WINDOW_MS }
        enqueue(OutboundDiscordMessage(channel, alert.detail.take(1_700), alert.deduplicationKey, alert.title.take(180)))
    }

    private fun enqueue(message: OutboundDiscordMessage) {
        if (!running.get()) return
        if (outbound.trySend(message).isFailure) {
            logger.warn("Discord delivery queue rejected a message key={}", message.deduplicationKey)
        }
    }

    private suspend fun installHandlers(client: Kord) {
        client.on<ReadyEvent> {
            logger.info("Discord bot ready guilds={}", client.guilds.count())
            sendToGeneral("Dodian Server is now online!")
            publishAlert(DiscordAlert(DiscordAlertKind.LIFECYCLE, "Discord bot online", "Kord gateway connected.", "discord-ready"))
        }
        client.on<ChatInputCommandInteractionCreateEvent> {
            val interaction = interaction as? GuildChatInputCommandInteraction ?: return@on
            val command = interaction.command.rootName
            val administrator = interaction.permissions.contains(Permission.Administrator)
            if (!DiscordCommandPolicy.isAllowed(command, administrator)) {
                handleAdmin(interaction, false, command) { "" }
                return@on
            }
            when (command) {
                "players" -> interaction.respondPublic { content = "There are currently ${PlayerRegistry.getPlayerCount()} players online." }
                "status" -> interaction.respondPublic { content = "Server online; ${PlayerRegistry.getPlayerCount()} players online." }
                "announce" -> handleAdmin(interaction, true, "announce") {
                    val message = interaction.command.strings["message"].orEmpty().trim()
                    if (message.isBlank()) "Announcement text is required." else {
                        sendToGeneral(message)
                        "Announcement queued."
                    }
                }
                "alert-test" -> handleAdmin(interaction, true, "alert-test") {
                    publishAlert(DiscordAlert(DiscordAlertKind.LIFECYCLE, "Staff alert test", "Requested by ${interaction.user.id}.", "alert-test-${interaction.user.id}"))
                    "Staff alert queued."
                }
            }
        }
    }

    private suspend fun registerCommands(client: Kord, guildId: Snowflake) {
        client.createGuildChatInputCommand(guildId, "players", "Show the online player count")
        client.createGuildChatInputCommand(guildId, "status", "Show bot and server status")
        client.createGuildChatInputCommand(guildId, "announce", "Post a server announcement") { string("message", "Announcement text") { required = true } }
        client.createGuildChatInputCommand(guildId, "alert-test", "Send a test staff alert")
    }

    private suspend fun handleAdmin(
        interaction: GuildChatInputCommandInteraction,
        administrator: Boolean,
        command: String,
        action: () -> String,
    ) {
        val result = if (administrator) action() else "Administrator permission is required."
        interaction.respondEphemeral { content = result }
        logger.info("DISCORD_ADMIN guild={} user={} command={} result={}", interaction.guildId, interaction.user.id, command, if (administrator) "accepted" else "denied")
    }

    private suspend fun deliveryLoop(client: Kord) {
        for (message in outbound) {
            var delivered = false
            repeat(MAX_ATTEMPTS) { attempt ->
                try {
                    val channel = client.getChannelOf<GuildMessageChannel>(message.channelId) ?: error("Configured Discord channel is unavailable")
                    channel.createMessage {
                        if (message.embedTitle == null) {
                            content = message.content
                        } else {
                            embed {
                                title = message.embedTitle
                                description = message.content
                            }
                        }
                    }
                    delivered = true
                    return@repeat
                } catch (failure: Throwable) {
                    if (attempt + 1 < MAX_ATTEMPTS) delay((attempt + 1) * 500L) else logger.warn("Discord delivery failed key={}", message.deduplicationKey, failure)
                }
            }
            if (!delivered) logger.warn("Discord message discarded after retries key={}", message.deduplicationKey)
        }
    }

    internal fun resetForTests() {
        running.set(false)
        kord = null
        recentAlerts.clear()
        configuration = configuredRuntime()
    }

    private fun configuredRuntime() = DiscordRuntimeConfiguration(discordToken, discordGuildId, discordChannelId, discordStaffAlertChannelId)
}

private fun String.asSnowflakeOrNull(): Snowflake? = trim().toULongOrNull()?.let(::Snowflake)
