package kr.alpha93.craftdsl.compiler

import java.io.File
import java.io.Serializable
import java.net.URL
import java.net.URLClassLoader
import kotlin.reflect.KClass
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptDependency
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.configurationDependencies
import kotlin.script.experimental.jvm.GetScriptingClassByClassLoader
import kotlin.script.experimental.jvm.JvmDependency

internal class ScriptingClassGetter(jar: File, loader: ClassLoader) : GetScriptingClassByClassLoader, Serializable {

    @Transient
    private val pluginJarFile: URL = jar.toURI().toURL()

    @Transient
    private val baseClassLoader: ClassLoader = loader

    @Transient
    private var dependencies: List<ScriptDependency>? = null

    @Transient
    private var classLoader: ClassLoader? = null

    @Synchronized
    override fun invoke(
        classType: KotlinType,
        contextClass: KClass<*>,
        hostConfiguration: ScriptingHostConfiguration
    ): KClass<*> =
        invoke(classType, contextClass.java.classLoader, hostConfiguration)

    @Synchronized
    override fun invoke(
        classType: KotlinType,
        contextClassLoader: ClassLoader?,
        hostConfiguration: ScriptingHostConfiguration
    ): KClass<*> {

        // Check if the class already loaded in the same context
        val fromClass = classType.fromClass
        if (fromClass != null) {
            if (fromClass.java.classLoader == null) return fromClass    // Root ClassLoader

            val chain = generateSequence(contextClassLoader) { it.parent }
            if (chain.any { it == fromClass.java.classLoader }) return fromClass
        }

        // Check and load dependencies
        val classpath = hostConfiguration[ScriptingHostConfiguration.configurationDependencies]
        if (dependencies == null) {
            dependencies = classpath
        } else if (dependencies != classpath) error("""
            configurationDependencies scripting configuration changed during execution:
                old: ${dependencies?.joinToString { (it as? JvmDependency)?.classpath.toString() }}
                new: ${classpath?.joinToString { (it as? JvmDependency)?.classpath.toString() }}
        """.trimIndent())

        // Initialise class loader
        if (classLoader == null) {
            val cp = dependencies?.flatMap { dependency ->
                check(dependency is JvmDependency) { "Unknown dependency type: $dependency" }
                return@flatMap dependency.classpath.map { it.toURI().toURL() }
            }?.takeUnless(Collection<URL>::isEmpty)?.toMutableList()?.apply { add(pluginJarFile) }

            classLoader = if (cp.isNullOrEmpty()) baseClassLoader else URLClassLoader(cp.toTypedArray(), baseClassLoader)
        }

        try {
            checkNotNull(classLoader) { "Class loader is not initialized" }
            return classLoader!!.loadClass(classType.typeName).kotlin
        } catch (e: Throwable) {
            throw IllegalArgumentException("Unable to load class: ${classType.typeName}", e)
        }

    }

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other !is ScriptingClassGetter -> false
        else -> other.dependencies == dependencies && other.baseClassLoader == baseClassLoader
    }

    override fun hashCode() = dependencies.hashCode() + 23 * baseClassLoader.hashCode() + 37

    companion object {
        private const val serialVersionUID = 1L
    }

}
