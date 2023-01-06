package pl.llp.aircasting.di.modules

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.SearchLocationResultFragment
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.SearchLocationFragment
import kotlin.reflect.KClass

@Module
abstract class FragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(SearchLocationResultFragment::class)
    internal abstract fun bindMapResultFragment(fragment: SearchLocationResultFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SearchLocationFragment::class)
    internal abstract fun bindSearchLocationFragment(fragment: SearchLocationFragment): Fragment
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class FragmentKey(val value: KClass<out Fragment>)