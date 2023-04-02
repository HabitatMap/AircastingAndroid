package pl.llp.aircasting.ui.view.screens.create_account

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import pl.llp.aircasting.AircastingApplication
import pl.llp.aircasting.data.api.params.CreateAccountBody
import pl.llp.aircasting.data.api.params.CreateAccountParams
import pl.llp.aircasting.data.api.response.UserResponse
import pl.llp.aircasting.data.api.services.ApiService
import pl.llp.aircasting.util.Settings
import retrofit2.Response


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class CreateAccountControllerTest {

    @Mock
    lateinit var apiService: ApiService

    @Mock
    lateinit var settings: Settings

    @Mock
    lateinit var appCompatActivity: AppCompatActivity

    @Mock
    lateinit var application: AircastingApplication

    private val testDispatcher = StandardTestDispatcher()
    private val testCoroutineScope = TestScope(testDispatcher)

    private val profileName = "name"
    private val password = "password"
    private val email = "email"
    private val username = "username"
    private val sendEmails = true
    private val token = "token"
    private val sessionStoppedAlert = true

    private lateinit var controller: CreateAccountController

    @Before
    fun setup() {
        whenever(appCompatActivity.startActivity(any())).thenAnswer { }
        whenever(appCompatActivity.application).thenAnswer { application }
        controller = CreateAccountController(
            appCompatActivity,
            mock(),
            false,
            testCoroutineScope,
            apiService,
            settings,
            mock(),
        )
    }

    @Test
    fun onCreateAccountClicked_callsApiService_withSpecifiedParameters() =
        testCoroutineScope.runTest {
            whenever(apiService.createAccount(any())).thenReturn(
                Response.success(
                    UserResponse(
                        email,
                        username,
                        token,
                        true
                    )
                )
            )
            val createAccountParams = CreateAccountParams(
                profileName,
                password,
                email,
                sendEmails
            )
            val createAccountBody = CreateAccountBody(createAccountParams)

            controller.onCreateAccountClicked(
                profileName,
                password,
                email,
                sendEmails
            )
            yield()

            verify(apiService).createAccount(eq(createAccountBody))
        }

    @Test
    fun onCreateAccountClicked_onSuccessfulCall_savesParametersToSettings_startsActivity() =
        testCoroutineScope.runTest {
            whenever(apiService.createAccount(any())).thenReturn(
                Response.success(
                    UserResponse(
                        email,
                        username,
                        token,
                        sessionStoppedAlert
                    )
                )
            )

            controller.onCreateAccountClicked(
                profileName,
                password,
                email,
                sendEmails
            )
            yield()

            inOrder(settings, appCompatActivity) {
                verify(settings).login(username, email, token, sessionStoppedAlert)
                verify(appCompatActivity).startActivity(any())
            }
        }
}