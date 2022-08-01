package pl.llp.aircasting.di.modules

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import pl.llp.aircasting.ui.view.fragments.search_follow_fixed_session.MapResultFragment
import kotlin.reflect.KClass

@Module
abstract class FragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(MapResultFragment::class)
    internal abstract fun bindMapResultFragment(fragment: MapResultFragment): Fragment
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class FragmentKey(val value: KClass<out Fragment>)