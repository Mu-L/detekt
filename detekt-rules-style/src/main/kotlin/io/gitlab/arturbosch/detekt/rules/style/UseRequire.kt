package io.gitlab.arturbosch.detekt.rules.style

import dev.detekt.api.ActiveByDefault
import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.psi.arguments
import dev.detekt.psi.isEmptyOrSingleStringArgument
import dev.detekt.psi.isEnclosedByConditionalStatement
import dev.detekt.psi.isIllegalArgumentException
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtThrowExpression

/**
 * Kotlin provides a much more concise way to check preconditions than to manually throw an
 * IllegalArgumentException.
 *
 * <noncompliant>
 * if (value == null) throw IllegalArgumentException("value should not be null")
 * if (value < 0) throw IllegalArgumentException("value is $value but should be at least 0")
 * </noncompliant>
 *
 * <compliant>
 * requireNotNull(value) { "value should not be null" }
 * require(value >= 0) { "value is $value but should be at least 0" }
 * </compliant>
 */
@ActiveByDefault(since = "1.21.0")
class UseRequire(config: Config) :
    Rule(
        config,
        "Use require() instead of throwing an IllegalArgumentException."
    ),
    RequiresAnalysisApi {

    override fun visitThrowExpression(expression: KtThrowExpression) {
        if (!expression.isIllegalArgumentException()) return
        if (expression.hasMoreExpressionsInBlock()) return

        if (expression.isEnclosedByConditionalStatement() &&
            expression.arguments.isEmptyOrSingleStringArgument()
        ) {
            report(Finding(Entity.from(expression), description))
        }
    }

    private fun KtThrowExpression.hasMoreExpressionsInBlock(): Boolean =
        (parent as? KtBlockExpression)?.run { statements.size > 1 } ?: false
}
