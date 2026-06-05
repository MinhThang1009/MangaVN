package com.example.mybookslibrary.ui.viewmodel

import javax.inject.Inject

class ReaderPageFileBuilder @Inject constructor() {
    operator fun invoke(
        chapterTitle: String,
        target: ReaderPageActionTarget,
    ): ReaderPageFile {
        val chapterSlug =
            chapterTitle
                .lowercase()
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .ifBlank { "chapter" }
        val extension =
            target.pageUrl
                .substringBefore('#')
                .substringBefore('?')
                .substringAfterLast('/', "")
                .substringAfterLast('.', "jpg")
                .takeIf { it.isNotBlank() }
                ?: "jpg"
        val pageNumber = target.pageIndex + 1
        val fileName = "${chapterSlug}_p${pageNumber}_${target.pageUrl.hashCode().toUInt().toString(16)}"
        return ReaderPageFile(fileName = fileName, extension = extension)
    }
}

data class ReaderPageFile(
    val fileName: String,
    val extension: String,
)
