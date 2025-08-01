package io.gitlab.arturbosch.detekt.rules.style

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.KotlinCoreEnvironmentTest
import dev.detekt.test.utils.KotlinEnvironmentContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@KotlinCoreEnvironmentTest
class NullableBooleanCheckSpec(val env: KotlinEnvironmentContainer) {
    val subject = NullableBooleanCheck(Config.empty)

    /**
     * The recommended replacement string for `?: [fallback]`.
     */
    private fun replacementForElvis(fallback: Boolean): String = if (fallback) "!= false" else "== true"

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `reports elvis in statement`(bool: Boolean) {
        val code = """
            import kotlin.random.Random
            
            fun nullableBoolean(): Boolean? = true.takeIf { Random.nextBoolean() }
            
            fun foo(): Boolean {
                return nullableBoolean() ?: $bool
            }
        """.trimIndent()

        val findings = subject.lintWithContext(env, code)
        assertThat(findings).hasSize(1)
        assertThat(findings).first().extracting { it.message }.isEqualTo(
            "The nullable boolean check `nullableBoolean() ?: $bool` should use " +
                "`${replacementForElvis(bool)}` rather than `?: $bool`"
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `reports elvis in if condition`(bool: Boolean) {
        val code = """
            import kotlin.random.Random
            
            fun nullableBoolean(): Boolean? = true.takeIf { Random.nextBoolean() }
            
            fun foo() {
                if (nullableBoolean() ?: $bool) println("foo")
            }
        """.trimIndent()

        val findings = subject.lintWithContext(env, code)
        assertThat(findings).hasSize(1)
        assertThat(findings).first().extracting { it.message }.isEqualTo(
            "The nullable boolean check `nullableBoolean() ?: $bool` should use " +
                "`${replacementForElvis(bool)}` rather than `?: $bool`"
        )
    }

    @Test
    fun `does not report for non-constant fallback`() {
        val code = """
            import kotlin.random.Random
            
            fun nullableBoolean(): Boolean? = true.takeIf { Random.nextBoolean() }
            
            fun foo(): Boolean {
                return nullableBoolean() ?: Random.nextBoolean()
            }
        """.trimIndent()

        assertThat(subject.lintWithContext(env, code)).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `does not report elvis for non-boolean statement with boolean default`(bool: Boolean) {
        val code = """
            import kotlin.random.Random
            
            fun nullableAny(): Any? = Unit.takeIf { Random.nextBoolean() }
            
            fun foo(): Any {
                return nullableAny() ?: $bool
            }
        """.trimIndent()

        assertThat(subject.lintWithContext(env, code)).isEmpty()
    }

    @Test
    fun `does not report non-boolean elvis`() {
        val code = """
            import kotlin.random.Random
            
            fun nullableInt(): Int? = 42.takeIf { Random.nextBoolean() }
            
            fun foo(): Int {
                return nullableInt() ?: 0
            }
        """.trimIndent()

        assertThat(subject.lintWithContext(env, code)).isEmpty()
    }

    @Test
    fun `does not report non-elvis binary expression`() {
        val code = """
            import kotlin.random.Random
            
            fun foo(): Boolean {
                return Random.nextBoolean() || false
            }
        """.trimIndent()

        assertThat(subject.lintWithContext(env, code)).isEmpty()
    }
}
