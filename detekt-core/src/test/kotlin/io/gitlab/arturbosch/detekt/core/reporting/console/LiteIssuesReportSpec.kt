package io.gitlab.arturbosch.detekt.core.reporting.console

import dev.detekt.api.testfixtures.TestDetektion
import dev.detekt.api.testfixtures.TestSetupContext
import dev.detekt.api.testfixtures.createIssue
import dev.detekt.api.testfixtures.createLocation
import dev.detekt.api.testfixtures.createRuleInstance
import io.gitlab.arturbosch.detekt.core.reporting.SuppressedIssueAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.absolute

class LiteIssuesReportSpec {
    private val basePath = Path("").absolute()
    private val subject = LiteIssuesReport().apply { init(TestSetupContext(basePath = basePath)) }

    @Test
    fun `reports non-empty issues`() {
        val location = createLocation()
        val detektion = TestDetektion(
            createIssue(createRuleInstance("SpacingAfterPackageDeclaration/id"), location),
            createIssue(createRuleInstance("UnnecessarySafeCall"), location),
        )
        assertThat(subject.render(detektion)).isEqualTo(
            """
                ${basePath.resolve(location.path)}:1:1: TestMessage [SpacingAfterPackageDeclaration/id]
                ${basePath.resolve(location.path)}:1:1: TestMessage [UnnecessarySafeCall]

            """.trimIndent()
        )
    }

    @Test
    fun `reports no issues`() {
        val detektion = TestDetektion()
        assertThat(subject.render(detektion)).isNull()
    }

    @Test
    fun `should not add auto corrected issues to report`() {
        val report = LiteIssuesReport()
        SuppressedIssueAssert.isReportNull(report)
    }
}
