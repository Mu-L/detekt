package io.gitlab.arturbosch.detekt.rules.bugs

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.psi.isMainFunction
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/**
 * Reports the usage of `System.exit()`, `Runtime.exit()`, `Runtime.halt()` and Kotlin's `exitProcess()`
 * when used outside the `main` function.
 * This makes code more difficult to test, causes unexpected behaviour on Android, and is a poor way to signal a
 * failure in the program. In almost all cases it is more appropriate to throw an exception.
 *
 * <noncompliant>
 * fun randomFunction() {
 *     val result = doWork()
 *     if (result == FAILURE) {
 *         exitProcess(2)
 *     } else {
 *         exitProcess(0)
 *     }
 * }
 * </noncompliant>
 *
 * <compliant>
 * fun main() {
 *     val result = doWork()
 *     if (result == FAILURE) {
 *         exitProcess(2)
 *     } else {
 *         exitProcess(0)
 *     }
 * }
 * </compliant>
 *
 */
class ExitOutsideMain(config: Config) :
    Rule(
        config,
        "Do not directly exit the process outside the `main` function. Throw an exception instead."
    ),
    RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        if (expression.getStrictParentOfType<KtNamedFunction>()?.isMainFunction() == true) return
        val fqName = analyze(expression) {
            expression.resolveToCall()?.singleFunctionCallOrNull()?.symbol?.callableId?.asSingleFqName()
        } ?: return

        if (fqName.asString() in exitCalls) {
            report(Finding(Entity.from(expression), description))
        }
    }

    companion object {
        val exitCalls = setOf(
            "kotlin.system.exitProcess",
            "java.lang.System.exit",
            "java.lang.Runtime.exit",
            "java.lang.Runtime.halt"
        )
    }
}
