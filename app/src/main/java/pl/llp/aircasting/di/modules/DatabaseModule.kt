package pl.llp.aircasting.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.data.local.migrations.*
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun providesAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "aircasting"
        )
            .fallbackToDestructiveMigration()
            .addMigrations(
                MIGRATION_16_17,
                MIGRATION_17_18,
                MIGRATION_18_19,
                MIGRATION_19_20,
                MIGRATION_20_21,
                MIGRATION_21_22,
                MIGRATION_22_23,
                MIGRATION_23_24,
                MIGRATION_24_25,
                MIGRATION_25_26,
                MIGRATION_26_27,
                MIGRATION_27_28,
                MIGRATION_28_29,
                MIGRATION_30_31,
                MIGRATION_31_32
            )
            .build()
    }
}