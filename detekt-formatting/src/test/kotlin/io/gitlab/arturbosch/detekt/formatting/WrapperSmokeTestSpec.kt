package io.gitlab.arturbosch.detekt.formatting

import dev.detekt.api.Config
import dev.detekt.test.assertThat
import dev.detekt.test.lint
import io.github.classgraph.ClassGraph
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class WrapperSmokeTestSpec {
    @ParameterizedTest(name = "for rule: {0}")
    @MethodSource("formattingRules")
    fun `smoke test`(subject: FormattingRule) {
        val result = subject.lint(
            """
                fun main() {
                    println("hello world!")
                }
                
            """.trimIndent()
        )

        assertThat(result).isEmpty()
    }

    fun formattingRules(): List<FormattingRule> =
        ClassGraph()
            .acceptPackages("io.gitlab.arturbosch.detekt.formatting.wrappers")
            .scan()
            .use { scanResult -> scanResult.getSubclasses(FormattingRule::class.java).loadClasses() }
            .map { it.getDeclaredConstructor(Config::class.java).newInstance(Config.empty) as FormattingRule }
}
