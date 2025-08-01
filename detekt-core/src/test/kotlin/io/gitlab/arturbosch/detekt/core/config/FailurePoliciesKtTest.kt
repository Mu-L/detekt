package io.gitlab.arturbosch.detekt.core.config

import dev.detekt.api.Severity
import dev.detekt.api.testfixtures.TestDetektion
import dev.detekt.api.testfixtures.createIssue
import dev.detekt.tooling.api.IssuesFound
import dev.detekt.tooling.api.spec.RulesSpec
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE
import org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE

class FailurePoliciesKtTest {

    @Nested
    inner class `Never Fail` {
        val subject = RulesSpec.FailurePolicy.NeverFail

        @Test
        fun `does not fail without an issue`() {
            val result = TestDetektion()

            subject.check(result)
        }

        @ParameterizedTest
        @EnumSource(value = Severity::class)
        fun `does not fail on issue with any severity`(issueSeverity: Severity) {
            val result = TestDetektion(createIssue(severity = issueSeverity))

            subject.check(result)
        }
    }

    @Nested
    inner class `Fail On Severity` {
        val subject = RulesSpec.FailurePolicy.FailOnSeverity(Severity.Warning)

        @Test
        fun `does not fail without an issue`() {
            val result = TestDetektion()

            subject.check(result)
        }

        @ParameterizedTest
        @EnumSource(value = Severity::class, names = ["Info"], mode = EXCLUDE)
        fun `fails on at least one issue at or above threshold`(issueSeverity: Severity) {
            val result = TestDetektion(createIssue(severity = issueSeverity))

            assertThatThrownBy { subject.check(result) }
                .isInstanceOf(IssuesFound::class.java)
        }

        @ParameterizedTest
        @EnumSource(value = Severity::class, names = ["Info"], mode = INCLUDE)
        fun `does not fail on issue below threshold`(issueSeverity: Severity) {
            val result = TestDetektion(createIssue(severity = issueSeverity))

            subject.check(result)
        }

        @Test
        fun `does not fail on suppressed issues`() {
            val result = TestDetektion(
                createIssue(severity = Severity.Error, suppressReasons = listOf("Because reasons"))
            )

            subject.check(result)
        }
    }
}
