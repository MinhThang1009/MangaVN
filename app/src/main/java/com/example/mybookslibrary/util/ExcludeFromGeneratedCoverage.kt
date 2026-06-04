package com.example.mybookslibrary.util

/**
 * Đánh dấu code KHÔNG unit-test được hợp lý (Android glue: notification, permission,
 * nhánh theo Build.VERSION) để JaCoCo loại khỏi báo cáo coverage.
 *
 * JaCoCo 0.8.2+ tự bỏ qua phần tử được annotate bằng annotation có "Generated" trong
 * tên (retention BINARY/RUNTIME). Chỉ dùng cho glue thật sự — KHÔNG để né test logic.
 */
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.CLASS,
)
annotation class ExcludeFromGeneratedCoverage
