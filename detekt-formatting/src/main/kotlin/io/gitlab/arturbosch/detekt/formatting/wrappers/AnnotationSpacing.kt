package io.gitlab.arturbosch.detekt.formatting.wrappers

import com.pinterest.ktlint.ruleset.standard.rules.AnnotationSpacingRule
import dev.detekt.api.ActiveByDefault
import dev.detekt.api.Config
import dev.detekt.api.internal.AutoCorrectable
import io.gitlab.arturbosch.detekt.formatting.FormattingRule

/**
 * See [ktlint docs](https://pinterest.github.io/ktlint/<ktlintVersion/>/rules/standard/#annotation-spacing) for documentation.
 */
@AutoCorrectable(since = "1.0.0")
@ActiveByDefault(since = "1.22.0")
class AnnotationSpacing(config: Config) : FormattingRule(
    config,
    "There should not be empty lines between an annotation and the object that it's annotating"
) {

    override val wrapping = AnnotationSpacingRule()
}
