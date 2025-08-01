package io.gitlab.arturbosch.detekt.rules.style

import dev.detekt.api.ActiveByDefault
import dev.detekt.api.Config
import dev.detekt.api.Configuration
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import dev.detekt.api.config
import dev.detekt.psi.isOverride
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtTypeArgumentList
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

/**
 * This rule detects usages of `Void` and reports them as forbidden.
 * The Kotlin type `Unit` should be used instead. This type corresponds to the `Void` class in Java
 * and has only one value - the `Unit` object.
 *
 * <noncompliant>
 * runnable: () -> Void
 * var aVoid: Void? = null
 * </noncompliant>
 *
 * <compliant>
 * runnable: () -> Unit
 * Void::class
 * </compliant>
 */
@ActiveByDefault(since = "1.21.0")
class ForbiddenVoid(config: Config) :
    Rule(
        config,
        "`Unit` should be used instead of `Void`."
    ),
    RequiresAnalysisApi {

    @Configuration("ignores void types in signatures of overridden functions")
    private val ignoreOverridden: Boolean by config(false)

    @Configuration("ignore void types as generic arguments")
    private val ignoreUsageInGenerics: Boolean by config(false)

    override fun visitTypeReference(typeReference: KtTypeReference) {
        val classId = analyze(typeReference) { typeReference.type.symbol?.classId } ?: return

        if (classId.asSingleFqName() == VOID_FQ_NAME) {
            if (ignoreOverridden && typeReference.isPartOfOverriddenSignature()) {
                return
            }
            if (ignoreUsageInGenerics && typeReference.isGenericArgument()) {
                return
            }
            report(Finding(Entity.from(typeReference), message = "'Void' should be replaced with 'Unit'."))
        }

        super.visitTypeReference(typeReference)
    }

    private fun KtTypeReference.isPartOfOverriddenSignature() =
        (isPartOfReturnTypeOfFunction() || isParameterTypeOfFunction()) &&
            getStrictParentOfType<KtNamedFunction>()?.isOverride() == true

    private fun KtTypeReference.isPartOfReturnTypeOfFunction() =
        getStrictParentOfType<KtNamedFunction>()
            ?.typeReference
            ?.anyDescendantOfType<KtTypeReference> { it == this }
            ?: false

    private fun KtTypeReference.isParameterTypeOfFunction() =
        getStrictParentOfType<KtParameter>() != null

    private fun KtTypeReference.isGenericArgument() =
        getStrictParentOfType<KtTypeArgumentList>() != null

    companion object {
        private val VOID_FQ_NAME = FqName("java.lang.Void")
    }
}
