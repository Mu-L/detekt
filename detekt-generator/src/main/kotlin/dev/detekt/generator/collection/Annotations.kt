package dev.detekt.generator.collection

import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import kotlin.reflect.KClass

fun KtAnnotated.isAnnotatedWith(annotation: KClass<out Annotation>): Boolean =
    annotationEntries.any { it.isOfType(annotation) }

fun KtAnnotated.getAnnotation(annotation: KClass<out Annotation>): KtAnnotationEntry? =
    annotationEntries.firstOrNull { it.isOfType(annotation) }

fun KtAnnotated.firstAnnotationParameter(annotation: KClass<out Annotation>): String =
    checkNotNull(firstAnnotationParameterOrNull(annotation))

fun KtAnnotated.firstAnnotationParameterOrNull(annotation: KClass<out Annotation>): String? =
    annotationEntries
        .firstOrNull { it.isOfType(annotation) }
        ?.firstParameterOrNull()

private fun KtAnnotationEntry.isOfType(annotation: KClass<out Annotation>) =
    shortName?.identifier == annotation.simpleName

private fun KtAnnotationEntry.firstParameterOrNull() =
    valueArguments
        .firstOrNull()
        ?.getArgumentExpression()
        ?.text
        ?.withoutQuotes()

internal fun String.withoutQuotes() = removeSurrounding(TRIPLE_QUOTES)
    .removeSurrounding(SINGLE_QUOTES)
    .replace(STRING_CONCAT_REGEX, "")

private const val SINGLE_QUOTES = "\""
private const val TRIPLE_QUOTES = "\"\"\""
private val STRING_CONCAT_REGEX = """"\s*\+[\n\s]*"""".toRegex()
