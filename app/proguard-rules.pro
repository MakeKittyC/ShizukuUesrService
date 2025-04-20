###########################
# 最好不要开混淆，如果需要，还请使用此配置
-keep class androidx.constraintlayout.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class com.google.android.material.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class moe.shizuku.** { *; }
-keep class rikka.** { *; }
-keep class app.compile.** { *; }
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile