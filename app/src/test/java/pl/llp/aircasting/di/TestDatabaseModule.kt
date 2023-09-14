package pl.llp.aircasting.di

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dagger.Module
import dagger.Provides
import pl.llp.aircasting.data.local.AppDatabase
import javax.inject.Singleton

@Module
class TestDatabaseModule {
    @Provides
    @Singleton
    fun providesAppDatabase(context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }
}