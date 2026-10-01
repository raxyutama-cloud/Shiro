# 🎮 Game Turbo - Complete Implementation Guide

## Overview

Game Turbo is a comprehensive Android performance optimization module integrated into Shiro. It provides:

- **CPU/GPU Frequency & Governor Control** via Shizuku
- **Per-Game Profiles** with granular settings
- **Real-time Monitoring Overlay** (FPS, CPU, GPU, RAM, Temp)
- **Quick Settings Tile** for instant toggle
- **Auto-Boost on Game Launch** detection
- **Scheduler Tuning** (fpsgo, uclamp, sched_boost)
- **Thermal Throttling Control**
- **RAM Cleaner & Background App Blocker**
- **Network Priority & DNS Boost**

---

## 📁 File Structure

```
android/app/src/main/java/com/mori/downloader/gameturbo/
├── di/
│   └── GameTurboModule.kt                 # Hilt DI module
├── core/
│   ├── GameTurboManager.kt                # Core orchestrator
│   ├── ShizukuHelper.kt                   # Shizuku integration (sysfs, shell)
│   ├── GameDetector.kt                    # Auto-detect installed games
│   ├── ProfileManager.kt                  # DataStore persistence
│   └── SystemTuner.kt                     # Apply/restore system state
├── service/
│   ├── BoostService.kt                    # Foreground boost service
│   ├── OverlayService.kt                  # Floating monitoring widget
│   └── GameMonitorService.kt              # Game launch/exit detection
├── tile/
│   └── GameTurboTileService.kt            # Quick Settings Tile
├── ui/
│   ├── MainActivity.kt                    # Compose entry point
│   ├── screen/
│   │   ├── GameListScreen.kt              # Game library with boost status
│   │   ├── ProfileScreen.kt               # Per-game settings (CPU, GPU, Display, Graphics)
│   │   ├── MonitorScreen.kt               # Real-time stats dashboard
│   │   └── SettingsScreen.kt              # Global config + Shizuku setup
│   ├── viewmodel/
│   │   ├── GameTurboViewModel.kt
│   │   ├── ProfileViewModel.kt
│   │   ├── MonitorViewModel.kt
│   │   └── SettingsViewModel.kt
│   └── widget/
│       ├── GameCard.kt                    # Game list item with boost indicator
│       ├── ProfileSection.kt              # Settings section wrapper
│       ├── SettingRow.kt                  # Setting row with control
│       └── StatCard.kt                    # CPU/GPU/Memory/Thermal summary cards
├── model/
│   ├── GameInfo.kt
│   ├── GameProfile.kt
│   ├── BoostConfig.kt
│   └── Stats.kt (CPUStats, GPUStats, MemoryStats, ThermalInfo, GameSessionStats)
├── receiver/
│   ├── BootReceiver.kt                    # Auto-start services on boot
│   └── PackageChangeReceiver.kt           # Refresh game list on install/uninstall
└── util/
    ├── SysfsUtils.kt                      # Sysfs read/write helpers
    ├── ShellUtils.kt                      # Shell command execution
    ├── ThermalUtils.kt                    # Thermal zone parsing
    └── Extensions.kt                      # Kotlin extensions
```

---

## ⚙️ Key Features Implementation

### 1. Shizuku Integration (`ShizukuHelper.kt`)

```kotlin
// CPU Control
suspend fun setAllCpuGovernors(governor: String): Result<Unit>
suspend fun setAllCpuFreqRange(minFreq: Int, maxFreq: Int): Result<Unit>

// GPU Control
suspend fun setGpuGovernor(governor: String): Result<Unit>
suspend fun setGpuFreqRange(minFreq: Int, maxFreq: Int): Result<Unit>

// Scheduler Tuning
suspend fun tuneScheduler(config: SchedulerConfig): Result<Unit>

// Thermal Control
suspend fun setThermalThrottling(enabled: Boolean): Result<Unit>

// App Ops / Background Control
suspend fun killBackgroundApps(excludePackages: List<String>): Result<Int>

// Network
suspend fun setGlobalDns(dns1: String, dns2: String): Result<Unit>

// Monitoring
suspend fun getCpuStats(): Result<CPUStats>
suspend fun getGpuStats(): Result<GPUStats>
suspend fun getMemoryStats(): Result<MemoryStats>
suspend fun getThermalInfo(): Result<ThermalInfo>
```

**Supported Sysfs Paths:**
- CPU: `/sys/devices/system/cpu/cpu*/cpufreq/`
- GPU: `/sys/class/kgsl/kgsl-3d0/devfreq/`, `/sys/class/devfreq/gpu/`
- Thermal: `/sys/class/thermal/thermal_zone*/`
- Scheduler: `/proc/sys/kernel/sched_*`, `/proc/sys/kernel/fpsgo/`

### 2. Per-Game Profiles (`GameProfile.kt`)

```kotlin
data class GameProfile(
    val packageName: String,
    // CPU
    val cpuGovernor: String = "performance",
    val cpuMinFreq: Int = 0,      // 0 = auto
    val cpuMaxFreq: Int = 0,
    // GPU
    val gpuGovernor: String = "performance",
    val gpuMinFreq: Int = 0,
    val gpuMaxFreq: Int = 0,
    // System
    val schedulerTuning: Boolean = true,
    val disableThermalThrottling: Boolean = false,
    val clearRamBeforeLaunch: Boolean = true,
    val blockBackgroundApps: Boolean = true,
    val networkPriority: Boolean = true,
    // Display
    val screenOffTimeout: Int = 0,
    val brightness: Int = -1,
    val volumeBoost: Float = 1.0f,
    // Graphics
    val fpsTarget: Int = 60,
    val resolutionScale: Float = 1.0f,
    val textureQuality: String = "high",
    val shadowsEnabled: Boolean = true,
    val antiAliasing: String = "msaa2x"
)
```

### 3. Real-time Monitoring (`MonitorViewModel.kt`)

Collects stats every 1 second:
- **CPU**: Usage %, frequency, governor, per-core load, temperature
- **GPU**: Frequency, governor, temperature, memory usage
- **Memory**: Total, available, used, cached, swap
- **Thermal**: Current temp, throttling level (0-4), CPU/GPU throttled flags
- **FPS**: Via Choreographer/SurfaceFlinger

### 4. Overlay Service (`OverlayService.kt`)

Floating widget with:
- Draggable position (persisted)
- FPS counter (large)
- CPU/GPU/Temp/RAM mini stats
- Click-through when not interacting
- Auto-hide when no game running

### 5. Quick Settings Tile (`GameTurboTileService.kt`)

- **Active state**: Green icon, "Game Turbo: ON"
- **Inactive state**: Gray icon, "Game Turbo: OFF"
- Single tap toggles global boost
- Auto-updates when boost starts/stops

### 6. Auto Game Detection (`GameMonitorService.kt`)

- Polls every 2 seconds
- Detects foreground game processes
- Triggers `onGameLaunched()` / `onGameExited()`
- Applies per-game profile automatically

---

## 🚀 Setup & Build

### 1. Add to `build.gradle.kts`

```kotlin
dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore-rx:1.1.1")
    implementation("io.reactivex.rxjava3:rxandroid:3.1.6")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui:1.6.5")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // Shizuku (check latest version)
    implementation("moe.shizuku:api:13.5.4")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
}
```

### 2. Add to `AndroidManifest.xml`

See `AndroidManifest_gameturbo_additions.xml` for required:
- Permissions (FOREGROUND_SERVICE, SYSTEM_ALERT_WINDOW, BIND_QUICK_SETTINGS_TILE, RECEIVE_BOOT_COMPLETED)
- Services (BoostService, OverlayService, GameMonitorService)
- TileService (GameTurboTileService)
- Receivers (BootReceiver, PackageChangeReceiver)
- Activity (MainActivity)

### 3. Add ProGuard Rules

```gradle
proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro', 'proguard-gameturbo.pro'
```

---

## 🔧 Usage

### Start Global Boost
```kotlin
val config = BoostConfig(
    enableCpuBoost = true,
    enableGpuBoost = true,
    enableSchedulerTuning = true,
    aggressiveMode = false
)
gameTurboManager.startBoost(config)
```

### Start Game Boost (Auto-applies profile)
```kotlin
gameTurboManager.startGameBoost(gameInfo)
```

### Stop Boost
```kotlin
gameTurboManager.stopBoost()
```

### Create/Update Game Profile
```kotlin
val profile = GameProfile(
    packageName = "com.example.game",
    cpuGovernor = "performance",
    cpuMaxFreq = 3000,  // MHz
    gpuGovernor = "performance",
    fpsTarget = 90,
    resolutionScale = 0.9f
)
profileManager.saveProfile(profile)
```

### Monitor Stats
```kotlin
viewModelScope.launch {
    monitorViewModel.cpuStats.collect { cpu ->
        // Update UI
    }
}
```

---

## 📱 Required Permissions

| Permission | Purpose |
|------------|---------|
| `FOREGROUND_SERVICE` | Boost/Monitor/Overlay services |
| `FOREGROUND_SERVICE_DATA_SYNC` | Data sync foreground type |
| `WAKE_LOCK` | Keep CPU awake during boost |
| `SYSTEM_ALERT_WINDOW` | Floating overlay widget |
| `BIND_QUICK_SETTINGS_TILE` | Quick Settings tile |
| `RECEIVE_BOOT_COMPLETED` | Auto-start on boot |
| `QUERY_ALL_PACKAGES` | Detect all installed games |

---

## 🎯 Shizuku Requirements

**Shizuku must be installed and granted permission:**
1. Install Shizuku from Play Store / GitHub
2. Run Shizuku setup (ADB or Root)
3. In Game Turbo Settings → "Setup Shizuku"
4. Grant permission when prompted

**Shizuku provides access to:**
- `/sys/devices/system/cpu/` (CPU freq/governor)
- `/sys/class/kgsl/kgsl-3d0/` (GPU freq/governor)
- `/proc/sys/kernel/` (scheduler tuning)
- `/sys/class/thermal/` (thermal zones)
- `cmd appops`, `cmd thermalservice`, `settings put`

---

## 🎨 UI Screens

| Screen | Description |
|--------|-------------|
| **Game List** | All detected games with boost status indicator |
| **Profile** | Per-game CPU/GPU/Display/Graphics settings |
| **Monitor** | Real-time charts: CPU, GPU, Memory, Thermal, FPS |
| **Settings** | Global boost config, Shizuku setup, Export/Import |

---

## 📦 Export/Import Config

```kotlin
// Export (saves to Downloads/Shiro/gameturbo_backup_<timestamp>.json)
settingsViewModel.exportConfig()

// Import (TODO: file picker)
settingsViewModel.importConfig()
```

Backup format:
```json
{
  "version": 1,
  "timestamp": 1700000000000,
  "boostConfig": { ... },
  "profiles": {
    "com.game.package": { ... }
  }
}
```

---

## 🔒 Safety Features

1. **Original State Restore**: Automatically saves/restores CPU/GPU governors, frequencies, screen timeout, brightness
2. **Thermal Protection**: Optional thermal throttling disable with warning
3. **Aggressive Mode Toggle**: Extra performance vs battery tradeoff
4. **Exclude Packages**: Don't kill system/essential apps during RAM clean
4. **Profile Validation**: Clamps values to hardware limits

---

## 📋 TODO / Future Enhancements

- [ ] FPS overlay via SurfaceFlinger/Choreographer
- [ ] Per-app network traffic monitoring
- [ ] Game-specific GPU driver properties (adb shell setprop)
- [ ] Thermal zone mapping per device
- [ ] Battery usage tracking per session
- [ ] Cloud profile sync
- [ ] Game library with cover art
- [ ] Macro/automation (Tasker/Intent integration)

---

## 📝 License

Part of Shiro project. MIT License.