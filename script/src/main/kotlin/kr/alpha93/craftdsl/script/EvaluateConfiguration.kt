package kr.alpha93.craftdsl.script

import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.scriptsInstancesSharing

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
internal object EvaluateConfiguration : ScriptEvaluationConfiguration({
    scriptsInstancesSharing(true)
})
