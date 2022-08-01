package pl.llp.aircasting.di.factories

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import javax.inject.Provider

class FragmentsFactory(
    private val providers: Map<String, Provider<Fragment>>
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return providers[className]?.get() ?: super.instantiate(classLoader, className)
    }

    companion object {
        operator fun invoke(
            providers: Map<Class<out Fragment>, Provider<Fragment>>
        ) : FragmentsFactory {
            return FragmentsFactory(
                providers.mapKeys { (fragmentClass, _) -> fragmentClass.name }
            )
        }
    }
}