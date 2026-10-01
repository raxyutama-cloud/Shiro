package com.mori.downloader.gameturbo.di

import android.app.Application
import android.content.Context
import com.mori.downloader.gameturbo.core.GameDetector
import com.mori.downloader.gameturbo.core.GameTurboManager
import com.mori.downloader.gameturbo.core.ProfileManager
import com.mori.downloader.gameturbo.core.ShizukuHelper
import com.mori.downloader.gameturbo.core.SystemTuner
import com.mori.downloader.gameturbo.service.BoostService
import com.mori.downloader.gameturbo.service.GameMonitorService
import com.mori.downloader.gameturbo.service.OverlayService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameTurboModule {
    
    @Provides
    @Singleton
    @ApplicationContext
    fun provideContext(application: Application): Context = application
    
    @Provides
    @Singleton
    fun provideShizukuHelper(@ApplicationContext context: Context): ShizukuHelper {
        return ShizukuHelper(context)
    }
    
    @Provides
    @Singleton
    fun provideGameDetector(@ApplicationContext context: Context): GameDetector {
        return GameDetector(context)
    }
    
    @Provides
    @Singleton
    fun provideProfileManager(@ApplicationContext context: Context): ProfileManager {
        return ProfileManager(context)
    }
    
    @Provides
    @Singleton
    fun provideSystemTuner(shizukuHelper: ShizukuHelper): SystemTuner {
        return SystemTuner(shizukuHelper)
    }
    
    @Provides
    @Singleton
    fun provideGameTurboManager(
        @ApplicationContext context: Context,
        shizukuHelper: ShizukuHelper,
        gameDetector: GameDetector,
        profileManager: ProfileManager,
        systemTuner: SystemTuner
    ): GameTurboManager {
        return GameTurboManager(context, shizukuHelper, gameDetector, profileManager, systemTuner)
    }
    
    // Services are AndroidEntryPoint, no need to provide
}

// EntryPoints for non-Hilt classes (BroadcastReceivers)
@dagger.hilt.EntryPoint
@InstallIn(SingletonComponent::class)
interface GameTurboEntryPoint {
    fun gameDetector(): GameDetector
    fun profileManager(): ProfileManager
    fun gameTurboManager(): GameTurboManager
}