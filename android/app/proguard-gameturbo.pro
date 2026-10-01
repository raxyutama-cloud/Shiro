# Game Turbo ProGuard Rules

# Shizuku
-keep class moe.shizuku.** { *; }
-dontwarn moe.shizuku.**

# Hilt
-keep class com.mori.downloader.gameturbo.** { *; }
-keep class dagger.hilt.** { *; }

# Room
-keep class com.mori.downloader.gameturbo.model.** { *; }

# Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# Compose
-keep class androidx.compose.** { *; }

# Material3
-keep class androidx.compose.material3.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }

# Datastore
-keep class androidx.datastore.** { *; }

# RxJava
-keep class io.reactivex.** { *; }

# Game Turbo specific
-keep class com.mori.downloader.gameturbo.core.** { *; }
-keep class com.mori.downloader.gameturbo.service.** { *; }
-keep class com.mori.downloader.gameturbo.tile.** { *; }
-keep class com.mori.downloader.gameturbo.receiver.** { *; }
-keep class com.mori.downloader.gameturbo.ui.** { *; }
-keep class com.mori.downloader.gameturbo.util.** { *; }
-keep class com.mori.downloader.gameturbo.model.** { *; }
-keep class com.mori.downloader.gameturbo.di.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable/Serializable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep R8/ProGuard from removing unused resources
-keep class **.R$* {
    <fields>;
}