package kr.alpha93.craftdsl.compiler

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kr.alpha93.craftdsl.script.ScriptDefinition
import java.io.File
import kotlin.reflect.KClass
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.getScriptingClass
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

@OptIn(ExperimentalCoroutinesApi::class)
class CompilerProvider(private val script: File, pluginJar: File, classLoader: ClassLoader) {

    private val config = ScriptingHostConfiguration { getScriptingClass(ScriptingClassGetter(pluginJar, classLoader)) }

    private val host = BasicJvmScriptingHost(config)

    private val compileConfig = createJvmCompilationConfigurationFromTemplate<ScriptDefinition>(config) {
        jvm {
            dependenciesFromClassContext(
                CompilerProvider::class, "script", "kotlin-scripting-dependencies", "kotlinx-datetime", "paper-api"
            )
        }
    }

    private val invokeConfig = createJvmEvaluationConfigurationFromTemplate<ScriptDefinition>(config)

    @OptIn(DelicateCoroutinesApi::class)
    private val compileThread = newSingleThreadContext("craftdsl-thread-compile")

    fun compile(): KClass<*> = runBlocking(compileThread) {
        val compiled = host.compiler(script.toScriptSource(), compileConfig).valueOrThrow()
        return@runBlocking compiled.getClass(invokeConfig).valueOrThrow()
    }

}
