# LookIA — R8 configuration
#
# R8 is enabled for release builds (AUDITORIA.md B-04). These rules cover the
# reflection-based frameworks the app uses. Keep this file in sync with the
# dependency list in app/build.gradle.kts.

# Preserve line numbers so Play Console de-obfuscates crash stack traces, then
# hide the original source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Room
# ---------------------------------------------------------------------------
# Room generates implementations that are resolved by name at runtime.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Entities are instantiated reflectively by the generated DAO code, and their
# constructor parameter names must survive for Room's column mapping.
-keepclassmembers class com.aistudio.lookia.data.model.** {
    <init>(...);
    <fields>;
}

# ---------------------------------------------------------------------------
# Kotlin / Coroutines
# ---------------------------------------------------------------------------
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# Kotlin metadata is required for reflection on data classes.
-keep class kotlin.Metadata { *; }

# ---------------------------------------------------------------------------
# Jetpack Compose
# ---------------------------------------------------------------------------
# The Compose compiler already emits the keep rules it needs; these only silence
# warnings about optional desugaring targets.
-dontwarn androidx.compose.**

# ---------------------------------------------------------------------------
# Enums
# ---------------------------------------------------------------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---------------------------------------------------------------------------
# Strip verbose logging from release builds
# ---------------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
}
