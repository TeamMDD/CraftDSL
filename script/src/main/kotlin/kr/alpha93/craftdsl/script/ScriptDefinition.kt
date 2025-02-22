package kr.alpha93.craftdsl.script

import kotlin.script.experimental.annotations.KotlinScript

/**
 * The definition of a CraftDSL script.
 */
@KotlinScript(
    displayName = "CraftDSL",
    fileExtension = "mc.kts",
    compilationConfiguration = CompileConfiguration::class,
    evaluationConfiguration = EvaluateConfiguration::class
)
abstract class ScriptDefinition private constructor() {

    init {
        throw UnsupportedOperationException("Script definition should not be instantiated.")
    }

}
