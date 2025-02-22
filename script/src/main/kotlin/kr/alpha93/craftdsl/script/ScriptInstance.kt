package kr.alpha93.craftdsl.script

import org.bukkit.Server
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

abstract class ScriptInstance(private val plugin: JavaPlugin, private val handle: Listener) {

    val logger: Logger
        get() = plugin.slF4JLogger

    val server: Server
        get() = plugin.server

    fun synchronized(block: () -> Unit) {
        server.scheduler.runTask(plugin, block)
    }

    fun schedule(delay: Long, async: Boolean, block: () -> Unit) {
        server.scheduler.runTaskLater(plugin, block, delay)
    }

    fun schedule(delay: Long, block: () -> Unit) = this.schedule(delay, false, block)
    fun schedule(async: Boolean, block: () -> Unit) = this.schedule(0, async, block)
    fun schedule(block: () -> Unit) = this.schedule(0, false, block)

    fun scheduleAsync(period: Duration, block: () -> Unit) {
        server.asyncScheduler.runAtFixedRate(plugin, { block() }, 0, period.toLong(DurationUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
    }

    fun scheduleAsync(block: () -> Unit) = this.scheduleAsync(5.milliseconds, block)

    fun <T : Event> on(event: KClass<T>, block: (T) -> Unit, priority: EventPriority) {
        @Suppress("UNCHECKED_CAST")
        server.pluginManager.registerEvent(event.java, handle, priority, { _, e -> block(e as T) }, plugin)
    }

    fun <T : Event> on(event: KClass<T>, block: (T) -> Unit) = this.on(event, block, EventPriority.NORMAL)

    inline fun <reified T : Event> on(noinline block: (T) -> Unit, priority: EventPriority) {
        this.on(T::class, block, priority)
    }

    inline fun <reified T : Event> on(noinline block: (T) -> Unit) = this.on(block, EventPriority.NORMAL)

}
