package pl.llp.aircasting.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import pl.llp.aircasting.di.factories.ViewModelFactory
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.edit.EditSessionBottomSheetViewModel
import pl.llp.aircasting.ui.viewmodel.CreateThresholdAlertBottomSheetViewModel
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import kotlin.reflect.KClass

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(EditSessionBottomSheetViewModel::class)
    internal abstract fun bindEditSessionBottomSheetViewModel(
        editSessionBottomSheetViewModel: EditSessionBottomSheetViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateThresholdAlertBottomSheetViewModel::class)
    internal abstract fun bindCreateThresholdAlertBottomSheetViewModel(
        createThresholdAlertBottomSheetViewModel: CreateThresholdAlertBottomSheetViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchFollowViewModel::class)
    internal abstract fun bindSearchFollowViewModel(searchFollowViewModel: SearchFollowViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)
