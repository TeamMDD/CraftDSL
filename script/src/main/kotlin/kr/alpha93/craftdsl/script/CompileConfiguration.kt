package kr.alpha93.craftdsl.script

import kotlinx.coroutines.runBlocking
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.*
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.jvmTarget
import kotlin.script.experimental.api.ScriptCollectedData as Collected
import kotlin.script.experimental.api.ScriptCompilationConfiguration as DSLCompileConfig
import kotlin.script.experimental.api.ScriptConfigurationRefinementContext as CompileContext
import kotlin.script.experimental.api.collectedAnnotations as annotations

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
internal object CompileConfiguration : DSLCompileConfig({

    baseClass(ScriptInstance::class)
    defaultImports(DependsOn::class, Repository::class, Import::class)

    jvm {
        jvmTarget("21")
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }

    refineConfiguration {
        onAnnotations(DependsOn::class, Repository::class, handler = AnnotationHandler::handleDependency)
        onAnnotations(Import::class, handler = AnnotationHandler::handleImports)
    }

}) {

    private object AnnotationHandler {

        @JvmStatic
        private val mvn = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())

        fun handleDependency(ctx: CompileContext): ResultWithDiagnostics<DSLCompileConfig> {
            val data = ctx.collectedData?.let { it[Collected.annotations] }?.takeUnless(Collection<*>::isEmpty)
                ?: return ctx.compilationConfiguration.asSuccess()

            return runBlocking { mvn.resolveFromScriptSourceAnnotations(data) }.onSuccess {
                ctx.compilationConfiguration.with { dependencies.append(JvmDependency(it)) }.asSuccess()
            }
        }

        fun handleImports(ctx: CompileContext): ResultWithDiagnostics<DSLCompileConfig> {
            val data = ctx.collectedData?.let { it[Collected.annotations] }?.takeUnless(Collection<*>::isEmpty)
                ?: return ctx.compilationConfiguration.asSuccess()

            val source = mutableSetOf<SourceCode>()
            data.forEach { (a, id) -> when (a) {
                is Import -> {
                    val rel = (ctx.script as FileBasedScriptSource).file.parentFile
                    a.path.forEach { source.add(rel.resolve(it).toScriptSource()) }
                }

                else -> return makeFailureResult("Unsupported annotation: $a", id)
            } }
            return ctx.compilationConfiguration.with { importScripts.append(source) }.asSuccess()
        }

    }

}
