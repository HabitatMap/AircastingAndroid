package pl.llp.aircasting.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import pl.llp.aircasting.di.factories.ViewModelFactory
import pl.llp.aircasting.ui.view.screens.dashboard.bottomsheet.menu_options.edit.EditSessionBottomSheetViewModel
import pl.llp.aircasting.ui.view.screens.settings.my_account.MyAccountViewModel
import pl.llp.aircasting.ui.viewmodel.CreateThresholdAlertBottomSheetViewModel
import pl.llp.aircasting.ui.viewmodel.SearchFollowViewModel
import pl.llp.aircasting.ui.viewmodel.SessionsViewModel
import kotlin.reflect.KClass

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(SessionsViewModel::class)
    internal abstract fun bindSessionsViewModel(
        viewModel: SessionsViewModel
    ): ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(MyAccountViewModel::class)
    internal abstract fun bindMyAccountViewModel(
        viewModel: MyAccountViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditSessionBottomSheetViewModel::class)
    internal abstract fun bindEditSessionBottomSheetViewModel(
        viewModel: EditSessionBottomSheetViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateThresholdAlertBottomSheetViewModel::class)
    internal abstract fun bindCreateThresholdAlertBottomSheetViewModel(
        viewModel: CreateThresholdAlertBottomSheetViewModel
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
