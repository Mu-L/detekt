package dev.detekt.test

import dev.detekt.api.Config
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.RequiresFullAnalysis
import dev.detekt.api.Rule
import dev.detekt.api.RuleSet
import dev.detekt.test.utils.KotlinAnalysisApiEngine
import dev.detekt.test.utils.KotlinEnvironmentContainer
import dev.detekt.test.utils.KotlinScriptEngine
import dev.detekt.test.utils.compileContentForTest
import io.gitlab.arturbosch.detekt.core.suppressors.isSuppressedBy
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.psi.KtFile

private val shouldCompileTestSnippets: Boolean =
    System.getProperty("compile-test-snippets", "false")!!.toBoolean()

private val shouldCompileTestSnippetsAa: Boolean =
    System.getProperty("compile-test-snippets-aa", "false")!!.toBoolean()

fun Rule.lint(
    @Language("kotlin") content: String,
    languageVersionSettings: LanguageVersionSettings = FakeLanguageVersionSettings(),
    compile: Boolean = true,
): List<Finding> {
    require(this !is RequiresFullAnalysis) {
        "${this.ruleName} requires full analysis so you should use lintWithContext instead of lint"
    }
    require(this !is RequiresAnalysisApi) {
        "${this.ruleName} requires Analysis APi so you should use lintWithContext instead of lint"
    }
    if (compile && shouldCompileTestSnippets) {
        KotlinScriptEngine.compile(content)
    }
    if (compile && shouldCompileTestSnippetsAa) {
        try {
            KotlinAnalysisApiEngine.compile(content)
        } catch (ex: RuntimeException) {
            if (!ex.isNoMatchingOutputFiles()) throw ex
        }
    }
    val ktFile = compileContentForTest(content)
    return visitFile(ktFile, languageVersionSettings = languageVersionSettings).filterSuppressed(this)
}

fun <T> T.lintWithContext(
    environment: KotlinEnvironmentContainer,
    @Language("kotlin") content: String,
    @Language("kotlin") vararg additionalContents: String,
    languageVersionSettings: LanguageVersionSettings = environment.configuration.languageVersionSettings,
    compile: Boolean = true,
): List<Finding> where T : Rule, T : RequiresFullAnalysis {
    if (compile && shouldCompileTestSnippets) {
        KotlinScriptEngine.compile(content)
    }
    if (compile && shouldCompileTestSnippetsAa) {
        try {
            KotlinAnalysisApiEngine.compile(content)
        } catch (ex: RuntimeException) {
            if (!ex.isNoMatchingOutputFiles()) throw ex
        }
    }
    val ktFile = compileContentForTest(content)
    val additionalKtFiles = additionalContents.mapIndexed { index, additionalContent ->
        compileContentForTest(additionalContent, "AdditionalTest$index.kt")
    }
    setBindingContext(environment.createBindingContext(listOf(ktFile) + additionalKtFiles))

    return visitFile(ktFile, languageVersionSettings).filterSuppressed(this)
}

fun <T> T.lintWithContext(
    environment: KotlinEnvironmentContainer,
    @Language("kotlin") content: String,
    @Language("kotlin") vararg dependencyContents: String,
    allowCompilationErrors: Boolean = false,
): List<Finding> where T : Rule, T : RequiresAnalysisApi {
    val ktFile = KotlinAnalysisApiEngine.compile(content, dependencyContents.toList(), allowCompilationErrors)
    return visitFile(ktFile, environment.configuration.languageVersionSettings).filterSuppressed(this)
}

fun Rule.lint(
    ktFile: KtFile,
    languageVersionSettings: LanguageVersionSettings = FakeLanguageVersionSettings(),
): List<Finding> {
    require(this !is RequiresFullAnalysis) {
        "${this.ruleName} requires full analysis so you should use lintWithContext instead of lint"
    }
    require(this !is RequiresAnalysisApi) {
        "${this.ruleName} requires Analysis Api so you should use lintWithContext instead of lint"
    }
    return visitFile(ktFile, languageVersionSettings = languageVersionSettings).filterSuppressed(this)
}

private fun List<Finding>.filterSuppressed(rule: Rule): List<Finding> =
    filterNot {
        it.entity.ktElement.isSuppressedBy(rule.ruleName.value, rule.aliases, RuleSet.Id("NoARuleSetId"))
    }

private val Rule.aliases: Set<String> get() = config.valueOrDefault(Config.ALIASES_KEY, emptyList<String>()).toSet()

private fun RuntimeException.isNoMatchingOutputFiles() =
    message?.contains("Compilation produced no matching output files") == true
