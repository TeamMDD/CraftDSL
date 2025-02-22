package kr.alpha93.ph.paper

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kr.alpha93.ph.paper.commands.PaperCommands
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import java.lang.Boolean.getBoolean as loadProperty

@Suppress("UnstableApiUsage")
abstract class KotlinPlugin protected constructor() : JavaPlugin() {

    val logger: Logger
        get() = this.slF4JLogger

    init {
        logger.info("Initializing server plugin ${pluginMeta.name} v${pluginMeta.version}")

        if (loadProperty("xyz.jpenilla.run-task")) {
            val ctx = LogManager.getContext(false) as LoggerContext
            ctx.configuration.getLoggerConfig(logger.name).level = Level.TRACE
            ctx.updateLoggers()

            check(logger.isTraceEnabled) { "Unable to enable trace logging" }
            logger.warn("Detected testing environment, enabling verbose")
        }

        logger.trace("Initializing: ${this::class.simpleName}")

        if (!dataPath.isDirectory()) dataPath.createDirectories()
        logger.trace("Plugin data directory is: {}", dataPath)

        this.init()
        logger.info("Successfully initialized ${pluginMeta.name}")
    }

    final override fun onLoad() {
        // Loading server plugin ${pluginMeta.name} v${pluginMeta.version}

        this.load()

        logger.trace("Registering commands")
        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            requireNotNull(it) { "Unable to retrieve command dispatcher" }
            this@KotlinPlugin.run { PaperCommands(this@KotlinPlugin, it.registrar()).register() }
        }

        logger.info("Successfully loaded ${pluginMeta.name}")
    }

    final override fun onEnable() {
        // Enabling ${pluginMeta.name} v${pluginMeta.version}

        this.enable()
        logger.info("Successfully enabled ${pluginMeta.name}")
    }

    final override fun onDisable() {
        // Enabling ${pluginMeta.name} v${pluginMeta.version}

        this.disable()
        logger.info("Successfully disabled ${pluginMeta.name}")
    }

    protected abstract fun init()

    protected abstract fun load()

    protected abstract fun enable()

    protected abstract fun disable()

    protected open fun PaperCommands.register() {
    }

}
