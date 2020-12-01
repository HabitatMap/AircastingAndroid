package io.lunarlogic.aircasting

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.lunarlogic.aircasting.lib.Settings
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class MyAccountTest {

    //TODO: how does ActivityRules work? do i need it here?

    @Inject
    lateinit var settings: Settings

    private fun setupDagger(){
        // todo: check other tests... are these methods there always the same??
    }

    @Before
    fun setup(){

    }

    @After
    fun cleanup(){

    }

}