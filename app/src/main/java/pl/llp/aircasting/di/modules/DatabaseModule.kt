package pl.llp.aircasting.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.local.AppDatabase
import pl.llp.aircasting.data.local.migrations.MIGRATION_16_17
import pl.llp.aircasting.data.local.migrations.MIGRATION_17_18
import pl.llp.aircasting.data.local.migrations.MIGRATION_18_19
import pl.llp.aircasting.data.local.migrations.MIGRATION_19_20
import pl.llp.aircasting.data.local.migrations.MIGRATION_20_21
import pl.llp.aircasting.data.local.migrations.MIGRATION_21_22
import pl.llp.aircasting.data.local.migrations.MIGRATION_22_23
import pl.llp.aircasting.data.local.migrations.MIGRATION_23_24
import pl.llp.aircasting.data.local.migrations.MIGRATION_24_25
import pl.llp.aircasting.data.local.migrations.MIGRATION_25_26
import pl.llp.aircasting.data.local.migrations.MIGRATION_26_27
import pl.llp.aircasting.data.local.migrations.MIGRATION_27_28
import pl.llp.aircasting.data.local.migrations.MIGRATION_28_29
import pl.llp.aircasting.data.local.migrations.MIGRATION_30_31
import pl.llp.aircasting.data.local.migrations.MIGRATION_31_32
import pl.llp.aircasting.data.local.migrations.MIGRATION_33_34
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
                MIGRATION_31_32,
                MIGRATION_33_34
            )
            .build()
    }
}