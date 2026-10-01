package com.mori.downloader.gameturbo.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PorterDuffColorFilter
import androidx.compose.ui.graphics.PorterDuffMode
import com.mori.downloader.gameturbo.model.GameInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Context extensions
fun Context.isShizukuAvailable(): Boolean {
    return try {
        moe.shizuku.manager.ShizukuManager.isShizukuAvailable(this) &&
        moe.shizuku.manager.ShizukuManager.checkSelfPermission(this) == 0
    } catch {
        false
    }
}

fun Context.requestShizukuPermission(): Boolean {
    return try {
        moe.shizuku.manager.ShizukuManager.requestPermission(this)
        true
    } catch {
        false
    }
}

fun Context.canDrawOverlays(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        android.provider.Settings.canDrawOverlays(this)
    } else true
}

fun Context.getOverlayPermissionIntent(): Intent {
    return Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
        data = Uri.parse("package:$packageName")
    }
}

// PackageManager extensions
fun PackageManager.getGameIcon(packageName: String): Drawable? {
    return try {
        getApplicationIcon(packageName)
    } catch {
        null
    }
}

fun PackageManager.isGamePackage(packageName: String): Boolean {
    return try {
        val info = getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        info.category == ApplicationInfo.CATEGORY_GAME
    } catch {
        false
    }
}

// Drawable to Compose ImageBitmap (placeholder)
fun Drawable.toComposeBitmap(): androidx.compose.ui.graphics.ImageBitmap? {
    // Implementation would use ImageBitmap.fromPixels or similar
    return null
}

// Color extensions
fun Color.withAlpha(alpha: Float): Color {
    return copy(alpha = alpha)
}

fun Color.darker(factor: Float = 0.8f): Color {
    return Color(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f),
        alpha = alpha,
        colorSpace = colorSpace
    )
}

fun Color.lighter(factor: Float = 1.2f): Color {
    return Color(
        red = (red * factor).coerceIn(0f, 1f),
        green = (green * factor).coerceIn(0f, 1f),
        blue = (blue * factor).coerceIn(0f, 1f),
        alpha = alpha,
        colorSpace = colorSpace
    )
}

// Result extensions
inline fun <T> Result<T>.getOrElse(default: () -> T): T = getOrElse { default() }
inline fun <T> Result<T>.getOrThrow(): T = getOrThrow()

// Flow extensions
fun <T> kotlinx.coroutines.flow.MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}

// String extensions
fun String.toIntOrNull(): Int? = try { toInt() } catch { null }
fun String.toFloatOrNull(): Float? = try { toFloat() } catch { null }
fun String.toLongOrNull(): Long? = try { toLong() } catch { null }

// Byte formatting
fun Long.formatBytes(): String = when {
    this >= 1_073_741_824 -> "%.1f GB".format(this / 1_073_741_824.0)
    this >= 1_048_576 -> "%.1f MB".format(this / 1_048_576.0)
    this >= 1_024 -> "%.1f KB".format(this / 1024.0)
    else -> "$this B"
}

// Frequency formatting
fun Int.formatFreq(): String = when {
    this >= 1_000_000 -> "%.1f GHz".format(this / 1_000_000.0)
    this >= 1_000 -> "%d MHz".format(this / 1_000)
    else -> "$this Hz"
}

// Temperature formatting
fun Float.formatTemp(): String = "%.1f°C".format(this)

// Percentage formatting
fun Float.formatPercent(decimals: Int = 1): String = "%.${decimals}f%%".format(this)

// Coroutine helpers
inline fun <T> kotlinx.coroutines.CoroutineScope.launchOnIO(block: suspend () -> T): kotlinx.coroutines.Job =
    launch(Dispatchers.IO) { block() }

inline fun <T> kotlinx.coroutines.CoroutineScope.launchOnMain(block: suspend () -> T): kotlinx.coroutines.Job =
    launch(Dispatchers.Main) { block() }