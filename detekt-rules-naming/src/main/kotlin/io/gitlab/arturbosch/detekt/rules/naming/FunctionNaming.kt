package io.gitlab.arturbosch.detekt.rules.naming

import dev.detekt.api.ActiveByDefault
import dev.detekt.api.Alias
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import dev.detekt.api.config
import dev.detekt.psi.isOverride
import io.gitlab.arturbosch.detekt.rules.naming.util.isContainingExcludedClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtUserType

/**
 * Reports function names that do not follow the specified naming convention.
 * One exception are factory functions used to create instances of classes.
 * These factory functions can have the same name as the class being created.
 */
@ActiveByDefault(since = "1.0.0")
@Alias("FunctionName")
class FunctionNaming(config: Config) : Rule(
    config,
    "Function names should follow the naming convention set in detekt's configuration."
) {

    @Configuration("naming pattern")
    private val functionPattern: Regex by config("[a-z][a-zA-Z0-9]*", String::toRegex)

    @Configuration("ignores functions in classes which match this regex")
    private val excludeClassPattern: Regex by config("$^", String::toRegex)

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        if (function.isOverride()) {
            return
        }

        val functionName = function.nameIdentifier?.text ?: return
        if (!function.isContainingExcludedClassOrObject(excludeClassPattern) &&
            !functionName.matches(functionPattern) &&
            functionName != function.returnTypeName()
        ) {
            report(
                Finding(
                    Entity.atName(function),
                    message = "Function names should match the pattern: $functionPattern"
                )
            )
        }
    }

    private fun KtNamedFunction.returnTypeName() = (typeReference?.typeElement as? KtUserType)?.referencedName

    companion object {
        const val FUNCTION_PATTERN = "functionPattern"
        const val EXCLUDE_CLASS_PATTERN = "excludeClassPattern"
    }
}
