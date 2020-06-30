package io.lunarlogic.aircasting.di

import android.app.Application
import io.lunarlogic.aircasting.lib.SettingsInterface

class FakeSettings(application: Application): SettingsInterface {
    override fun getAuthToken(): String? {
        return "fake!"
    }
}
