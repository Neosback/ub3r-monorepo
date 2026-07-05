package net.dodian.uber.game.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dodian.uber.game.engine.config.discordToken
import net.dodian.uber.game.engine.systems.world.player.PlayerRegistry
import org.slf4j.LoggerFactory
import java.util.concurrent.ForkJoinPool

object DiscordService : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(DiscordService::class.java)

    @Volatile
    private var jda: JDA? = null

    fun start() {
        val token = discordToken
        if (token.isBlank()) {
            logger.info("Discord token is blank. Discord bot will not start.")
            return
        }

        logger.info("Initializing Discord Bot...")
        ForkJoinPool.commonPool().execute {
            try {
                val jdaInstance = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(this)
                    .setActivity(Activity.playing("Dodian"))
                    .build()
                this.jda = jdaInstance
            } catch (e: Exception) {
                logger.error("Failed to start Discord bot", e)
            }
        }
    }

    override fun onReady(event: ReadyEvent) {
        logger.info("Discord bot initialized and ready.")
        val guilds = event.jda.guilds
        if (guilds.isEmpty()) {
            logger.warn("The Discord bot is not in any servers (guilds). Please invite the bot to your server using the OAuth2 generator in the Discord Developer Portal!")
        } else {
            logger.info("Discord bot is currently connected to ${guilds.size} server(s):")
            for (guild in guilds) {
                logger.info("  - ${guild.name} (ID: ${guild.id})")
            }
        }
        sendToGeneral("Dodian Server is now online!")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) {
            return
        }

        val message = event.message.contentRaw.trim()
        if (message.equals("!players", ignoreCase = true)) {
            val playerCount = PlayerRegistry.getPlayerCount()
            event.channel.sendMessage("There are currently $playerCount players online!").queue()
        }
    }

    @JvmStatic
    fun sendToGeneral(message: String) {
        val activeJda = jda ?: return
        ForkJoinPool.commonPool().execute {
            try {
                // Try sending directly to the configured channel ID first
                val targetChannelId = net.dodian.uber.game.engine.config.discordChannelId
                val specificChannel = if (targetChannelId.isNotBlank()) activeJda.getTextChannelById(targetChannelId) else null
                if (specificChannel != null) {
                    specificChannel.sendMessage(message).queue()
                    logger.info("Sent announcement to configured channel ID $targetChannelId")
                    return@execute
                }

                // Fallback: search general by name or first channel
                for (guild in activeJda.guilds) {
                    val channels = guild.getTextChannelsByName("general", true)
                    val channel = channels.firstOrNull() ?: guild.textChannels.firstOrNull()
                    if (channel != null) {
                        channel.sendMessage(message).queue()
                        logger.debug("Sent announcement to channel '{}' in guild '{}'", channel.name, guild.name)
                    } else {
                        logger.warn("Could not find a text channel to post announcement in guild '{}'", guild.name)
                    }
                }
            } catch (e: Exception) {
                logger.error("Error sending message to general channel", e)
            }
        }
    }
}
