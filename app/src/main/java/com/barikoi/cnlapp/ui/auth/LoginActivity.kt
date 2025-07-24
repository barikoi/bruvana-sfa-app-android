package com.barikoi.cnlapp.ui.auth

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.barikoitrace.callback.BarikoiTraceUserCallback
import com.barikoi.barikoitrace.models.BarikoiTraceError
import com.barikoi.barikoitrace.models.BarikoiTraceUser
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.databinding.ActivityLoginBinding
import com.barikoi.cnlapp.ui.auth.vm.LoginViewModel
import com.barikoi.cnlapp.ui.main.MainActivity
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.hideKeyboard
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.protocol.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    private lateinit var pd: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pd = ProgressDialog(this)
        pd.setMessage("Authenticating...")

        startLoginObserve()

        binding.btnLogin.setHapticClickListener {
            hideKeyboard()
            val userId = binding.etSRCode.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (userId.isEmpty() || password.isEmpty()) {
                toast("Please enter SR Code and Password")
                return@setHapticClickListener
            }
            viewModel.login(userId, password)
        }

        binding.etSRCode.doOnTextChanged { text, _, _, _ ->
            viewModel.userIdStateFlow.value = text.toString()
        }

        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.passwordStateFlow.value = text.toString()
        }

        binding.etSRCode.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, binding.btnLogin.bottom)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoginInfoValid.collectLatest {
                binding.btnLogin.isEnabled = it
            }
        }

//        if (FLAVOR == "staging") {
//            binding.etSRCode.setText("111888888")
//            binding.etPassword.setText("12345678")
//        }
    }

    private fun startLoginObserve() {
        lifecycleScope.launch {
            viewModel.loginResponse.observe(this@LoginActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startLoginObserve::Empty")
                        pd.dismiss()
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startLoginObserve::Error DATA${it.data}")
                        AppLogger.log("startLoginObserve::Error ${it.error}")
                        pd.dismiss()

                        if (it.data != null) {
                            toast(it.data.message)
                            return@observe
                        }

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startLoginObserve::Loading")
                        pd.show()
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startLoginObserve:: Success ${it.data}")
                        pd.dismiss()

                        if (it.data?.message == "Password Incorrect") {
                            toast(it.data.message)
                            return@observe
                        }

                        it.data?.let { data ->
                            sharePrefUtils.saveString(Api.TOKEN, data.token)

                            sharePrefUtils.saveString(Api.EMAIL, data.user.email ?: "")
                            sharePrefUtils.saveString(Api.NAME, data.user.userName)
                            sharePrefUtils.saveString(Api.USER_ID, data.user.id.toString())

                            sharePrefUtils.saveString(
                                Api.USER_TYPE,
                                data.user.designation!!
                            )

                            sharePrefUtils.saveString(Api.PHONE, data.user.phone)

                            sharePrefUtils.saveString(
                                Constants.DB_HOUSE_ID,
                                data.user.dbHouseId.toString()
                            )

                            sharePrefUtils.saveString(
                                Constants.DB_HOUSE,
                                data.user.dbHouse?.dbHouseName ?: ""
                            )

                            sharePrefUtils.saveString(
                                Constants.AREA_ID,
                                data.user.areaId.toString()
                            )
                            sharePrefUtils.saveString(
                                Constants.TERRITORY_ID,
                                data.user.territoryId.toString()
                            )
                            sharePrefUtils.saveString(
                                Constants.REGION_ID,
                                data.user.regionId.toString()
                            )
                            sharePrefUtils.saveString(
                                Constants.NATION_ID,
                                data.user.nationId.toString()
                            )

                            sharePrefUtils.saveString(
                                Api.EMPLOYEE_ID,
                                data.user.employeeId
                            )
                            sharePrefUtils.saveString(
                                Api.TRACE_GROUP_NAME,
                                data.user.groupName ?: ""
                            )

                            sharePrefUtils.saveString(
                                Api.TRACE_GROUP_ID,
                                data.user.groupId ?: ""
                            )

                        }

                        val phone =
                            if (it.data?.user?.phone?.length == 10) "0${it.data.user.phone}" else it.data?.user?.phone

                        BarikoiTrace.setOrCreateUser(
                            it.data?.user?.userName,
                            it.data?.user?.email,
                            phone,
                            object : BarikoiTraceUserCallback {
                                override fun onFailure(barikoiError: BarikoiTraceError) {
                                    Log.d(
                                        "BarikoiTrace",
                                        "User created onFailure: ${barikoiError.message}"
                                    )
                                }

                                override fun onSuccess(traceUser: BarikoiTraceUser) {
                                    Log.d("BarikoiTrace", "User created: $traceUser")
                                }
                            })

                        SentryAndroid.init(this@LoginActivity) { options: SentryAndroidOptions ->
                            // Add a callback that will be used before the event is sent to Sentry.
                            // With this callback, you can modify the event or, when returning null, also discard the event.
                            options.beforeSend =
                                SentryOptions.BeforeSendCallback { event: SentryEvent, hint: Any? ->
                                    val userSentry = User()
                                    userSentry.id = it.data?.user?.employeeId
                                    userSentry.email = it.data?.user?.employeeId
                                    userSentry.username = it.data?.user?.userName
                                    event.user = userSentry
                                    event
                                }
                        }

                        toast("Login Successful")

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}