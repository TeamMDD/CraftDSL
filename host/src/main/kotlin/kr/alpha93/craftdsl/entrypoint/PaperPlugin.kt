package kr.alpha93.craftdsl.entrypoint

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kr.alpha93.craftdsl.compiler.CompilerProvider
import kr.alpha93.dokdo.Holder
import kr.alpha93.ph.paper.KotlinPlugin
import kr.alpha93.ph.paper.commands.PaperCommands
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.io.path.outputStream
import kotlin.reflect.KClass
import kotlin.script.experimental.jvm.util.classPathFromTypicalResourceUrls

internal class PaperPlugin : KotlinPlugin() {

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val loaderThread = newSingleThreadContext("craftdsl-thread-loader").executor

    private var compiler by Holder<CompilerProvider>()

    private var instance: Any? = null

    override fun init() {
        System.setProperty(
            "kotlin.script.classpath",
            classLoader.classPathFromTypicalResourceUrls().joinToString(":") { it.toString() }
        )
    }

    override fun load() {
        val scriptSrc = dataPath.resolve("src")

        if (!scriptSrc.isDirectory()) scriptSrc.createDirectories()
        logger.trace("Script source directory is: {}", scriptSrc)

        val mainScript = scriptSrc.resolve("main.mc.kts")
        if (mainScript.notExists()) {
            val example = classLoader.getResourceAsStream("src-examples/main.mc.kts")
            checkNotNull(example) { "Unable to find example main.mc.kts in plugin resources" }

            example.copyTo(mainScript.outputStream())
            logger.trace("Copied example main.mc.kts")
        }

        logger.trace("Found main script: {}", mainScript)

        compiler = CompilerProvider(mainScript.toFile(), file, classLoader)
    }

    override fun enable() {
        compiler.loadMain()
    }

    override fun disable() {
    }

    override fun PaperCommands.register() {
        DSLCommand.loader = this@PaperPlugin
        register(DSLCommand::class)
    }

    private fun Class<*>.createInstance(): Any {
        val new = constructors.single()

        val params = mutableListOf<Any>()
        params.add(this@PaperPlugin)
        params.add(Companion)

        if (new.parameters.size > 2) new.parameters.slice(2 until new.parameters.size).forEach { param ->
            params.add(param.type.createInstance())
        }

        return new.newInstance(*params.toTypedArray())
    }

    private fun CompilerProvider.loadMain() {
        logger.info("Compiling scripts")
        val start = System.currentTimeMillis()

        val clazz: KClass<*> = try {
            this.compile()
        } catch (t: Throwable) {
            logger.warn("Failed to compile script!", t)
            return
        }

        logger.info("Successfully compiled scripts! ({}ms)", System.currentTimeMillis() - start)

        try {
            if (instance != null) {
                logger.info("Disabling previous instance...")
                server.scheduler.cancelTasks(this@PaperPlugin)
                server.asyncScheduler.cancelTasks(this@PaperPlugin)
                HandlerList.unregisterAll(Companion)
            }
        } catch (t: Throwable) {
            logger.warn("Failed to disable previous instance!", t)
            return
        }

        logger.info("Loading scripts...")
        instance = try {
            clazz.java.createInstance()
        } catch (t: Throwable) {
            logger.warn("Failed to load script!", t)
            instance = null
            return
        }
        logger.info("Successfully enabled scripts!")
    }

    fun loadAsynchronously() = loaderThread.execute { compiler.loadMain() }

    private companion object : Listener

}
