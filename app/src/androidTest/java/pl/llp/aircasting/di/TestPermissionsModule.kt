package pl.llp.aircasting.di

import org.mockito.Mockito
import pl.llp.aircasting.di.modules.PermissionsModule
import pl.llp.aircasting.util.helpers.permissions.PermissionsManager

class TestPermissionsModule : PermissionsModule() {
    override fun providesPermissionsManager(): PermissionsManager {
        return Mockito.mock(PermissionsManager::class.java)
    }
}
