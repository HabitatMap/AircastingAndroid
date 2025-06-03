package pl.llp.aircasting.utilities

import com.appmattus.kotlinfixture.Context
import com.appmattus.kotlinfixture.decorator.constructor.ConstructorStrategy
import com.appmattus.kotlinfixture.decorator.constructor.constructorStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

val dataClassConstructorStrategy = object : ConstructorStrategy {
    override fun constructors(context: Context, obj: KClass<*>): Collection<KFunction<*>> {
        return listOf(obj.primaryConstructor!!)
    }
}
val dataClassFixture = kotlinFixture {
    constructorStrategy(dataClassConstructorStrategy)
}