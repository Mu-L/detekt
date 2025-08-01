package dev.detekt.rules.empty

import dev.detekt.api.Config
import dev.detekt.test.TestConfig
import dev.detekt.test.assertThat
import dev.detekt.test.lint
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val IGNORE_OVERRIDDEN = "ignoreOverridden"

class EmptyFunctionBlockSpec {

    private val subject = EmptyFunctionBlock(Config.empty)

    @Test
    fun `should flag function with protected modifier`() {
        val code = """
            class A {
                protected fun stuff() {}
            }
        """.trimIndent()
        assertThat(subject.lint(code)).hasStartSourceLocation(2, 27)
    }

    @Test
    fun `should not flag function with open modifier`() {
        val code = """
            open class A {
                open fun stuff() {}
            }
        """.trimIndent()
        assertThat(subject.lint(code)).isEmpty()
    }

    @Test
    fun `should not flag a default function in an interface`() {
        val code = """
            interface I {
                fun stuff() {}
            }
        """.trimIndent()
        assertThat(subject.lint(code)).isEmpty()
    }

    @Test
    fun `should flag the nested empty function`() {
        val code = """
            fun a() {
                fun b() {}
            }
        """.trimIndent()
        assertThat(subject.lint(code)).hasStartSourceLocation(2, 13)
    }

    @Nested
    inner class `some overridden functions` {

        val code = """
            fun empty() {}
            
            open class Base {
                open fun stuff() {}
            }
            
            class A : Base() {
                override fun stuff() {}
            }
            
            class B : Base() {
                override fun stuff() {
                    TODO("Implement this")
                }
            }
            
            class C : Base() {
                override fun stuff() {
                    // this is necessary...
                }
            }
        """.trimIndent()

        @Test
        fun `should flag empty block in overridden function`() {
            assertThat(subject.lint(code)).hasSize(2)
        }

        @Test
        fun `should not flag overridden functions`() {
            val config = TestConfig(IGNORE_OVERRIDDEN to "true")
            assertThat(EmptyFunctionBlock(config).lint(code)).hasStartSourceLocation(1, 13)
        }
    }

    @Nested
    inner class `some overridden functions when implementing interfaces` {
        val code = """
            private interface Listener {
                fun listenThis()
            
                fun listenThat()
            }
            
            private class AnimationEndListener : Listener {
                override fun listenThis() {
                    // no-op
                }
            
                override fun listenThat() {
            
                }
            }
        """.trimIndent()

        @Test
        fun `should not flag overridden functions with commented body`() {
            assertThat(subject.lint(code)).hasStartSourceLocation(12, 31)
        }

        @Test
        fun `should not flag overridden functions with ignoreOverridden`() {
            val config = TestConfig(IGNORE_OVERRIDDEN to "true")
            assertThat(EmptyFunctionBlock(config).lint(code)).isEmpty()
        }
    }
}
