package net.dodian.uber.game.api.plugin

import com.google.common.reflect.ClassPath
import net.dodian.uber.game.api.plugin.skills.SkillPlugin
import net.dodian.uber.game.engine.systems.interaction.commands.CommandContent
import net.dodian.uber.game.content.items.ItemContent
import net.dodian.uber.game.content.npcs.NpcContentDefinition
import net.dodian.uber.game.content.npcs.NpcModule
import net.dodian.uber.game.content.objects.ObjectContent
import net.dodian.uber.game.content.shop.plugin.ShopPlugin
import net.dodian.uber.game.content.ui.buttons.InterfaceButtonContent
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * Runtime-scanned plugin module index.
 *
 * Replaces the former KSP-generated `GeneratedPluginModuleIndex` with classpath
 * scanning at startup. All Kotlin `object` declarations that implement one of the
 * known plugin interfaces are discovered automatically.
 *
 * Inspired by Luna's runtime plugin system — no code generation step required.
 */
object ContentModuleIndex {
    private val logger = LoggerFactory.getLogger(ContentModuleIndex::class.java)

    /** Content packages to scan for plugin objects. */
    private val SCAN_PACKAGES = listOf(
        "net.dodian.uber.game.content",
        "net.dodian.uber.game.engine.systems.interaction",
        "net.dodian.uber.game.engine.event.bootstrap",
    )

    @JvmField val interfaceButtons: List<InterfaceButtonContent>
    @JvmField val objectContents: List<Pair<String, ObjectContent>>
    @JvmField val itemContents: List<ItemContent>
    @JvmField val commandContents: List<CommandContent>
    @JvmField val npcContents: List<NpcContentDefinition>
    @JvmField val skillPlugins: List<SkillPlugin>
    @JvmField val shopPlugins: List<ShopPlugin>
    @JvmField val eventBootstraps: List<() -> Unit>
    @JvmField val contentBootstraps: List<ContentBootstrap>

    init {
        val classPath = ClassPath.from(Thread.currentThread().contextClassLoader)
        val scannedClasses = SCAN_PACKAGES.flatMap { pkg ->
            classPath.getTopLevelClassesRecursive(pkg).mapNotNull { info ->
                try {
                    info.load()
                } catch (e: Throwable) {
                    logger.warn("Failed to load class {}: {}", info.name, e.message)
                    null
                }
            }
        }

        val buttons = mutableListOf<InterfaceButtonContent>()
        val objects = mutableListOf<Pair<String, ObjectContent>>()
        val items = mutableListOf<ItemContent>()
        val commands = mutableListOf<CommandContent>()
        val npcs = mutableListOf<NpcContentDefinition>()
        val skills = mutableListOf<SkillPlugin>()
        val shops = mutableListOf<ShopPlugin>()
        val events = mutableListOf<() -> Unit>()
        val bootstraps = mutableListOf<ContentBootstrap>()

        for (clazz in scannedClasses) {
            // Only process Kotlin objects (singletons)
            val instance = try {
                clazz.kotlin.objectInstance ?: continue
            } catch (_: Throwable) {
                continue
            }

            when (instance) {
                is InterfaceButtonContent -> buttons += instance
                is ObjectContent -> objects += (clazz.simpleName to instance)
                is ItemContent -> items += instance
                is CommandContent -> commands += instance
                is SkillPlugin -> skills += instance
                is ShopPlugin -> shops += instance
                is ContentBootstrap -> bootstraps += instance
            }

            // NpcModule objects expose a `definition` property
            if (instance is NpcModule) {
                npcs += instance.definition
            }

            // Event bootstraps: objects in engine.event.bootstrap with a bootstrap() method,
            // discovered by naming convention (excludes CoreEventBusBootstrap itself).
            if (clazz.name.startsWith("net.dodian.uber.game.engine.event.bootstrap") &&
                clazz.simpleName.endsWith("Bootstrap") &&
                clazz.simpleName != "CoreEventBusBootstrap"
            ) {
                val method: Method? = try {
                    clazz.getMethod("bootstrap")
                } catch (_: NoSuchMethodException) {
                    null
                }
                if (method != null) {
                    events += { method.invoke(instance) }
                }
            }
        }

        interfaceButtons = buttons.toList()
        objectContents = objects.toList()
        itemContents = items.toList()
        commandContents = commands.toList()
        npcContents = npcs.toList()
        skillPlugins = skills.toList()
        shopPlugins = shops.toList()
        eventBootstraps = events.toList()
        contentBootstraps = bootstraps.toList()

        logger.info(
            "Plugin index scanned: buttons={}, objects={}, items={}, commands={}, npcs={}, skills={}, shops={}, events={}, bootstraps={}",
            interfaceButtons.size, objectContents.size, itemContents.size, commandContents.size,
            npcContents.size, skillPlugins.size, shopPlugins.size, eventBootstraps.size, contentBootstraps.size,
        )
    }
}
