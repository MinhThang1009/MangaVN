# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Giữ line number để crash stack trace đọc được sau khi R8 obfuscate;
# ẩn tên file gốc (chỉ còn "SourceFile:line") — đủ debug với mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firestore deserialize POJO bằng reflection trên TÊN field/getter (toObject/toObjects).
# R8 rename field HOẶC getter -> map sai -> data rỗng + rules check field-name fail
# (vỡ im lặng trên bản release ký; debug không minify nên không lộ).
# Giữ NGUYÊN cả class (field + getter + no-arg constructor) cho mọi model Firestore —
# theo khuyến nghị Firebase docs cho POJO. Chỉ ~3 class nhỏ, ảnh hưởng size không đáng kể.
-keep class com.example.mybookslibrary.data.remote.models.** { *; }