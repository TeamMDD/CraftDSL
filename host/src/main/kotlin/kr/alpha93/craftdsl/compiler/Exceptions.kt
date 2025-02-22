package kr.alpha93.craftdsl.compiler

import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic.Severity

abstract class ScriptProcessingException internal constructor(
    val script: File,
    val severity: Severity,
    val throwable: Throwable,
) : Exception() {

    internal constructor(script: File, severity: Severity, message: String) :
            this(script, severity, RuntimeException(message))

    init {
        addSuppressed(throwable)
    }

}

class ScriptCompilationException(
    script: File, severity: Severity, throwable: Throwable
) : ScriptProcessingException(script, severity, throwable) {

    internal constructor(script: File, severity: Severity, message: String) :
            this(script, severity, RuntimeException(message))

}
