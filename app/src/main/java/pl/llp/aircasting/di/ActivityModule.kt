package pl.llp.aircasting.di

import android.app.Activity
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(
    private val mActivity: Activity
) {
    @Provides
    fun providesActivity(): Activity = mActivity
}