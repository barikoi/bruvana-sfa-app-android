package com.barikoi.cnlapp.Activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.barikoitrace.callback.BarikoiTraceUserCallback
import com.barikoi.barikoitrace.models.BarikoiTraceError
import com.barikoi.barikoitrace.models.BarikoiTraceUser
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.databinding.ActivityLoginBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.SharePrefUtils
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.protocol.User
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private var loginSuccess: Boolean? = false
    var pd: ProgressDialog? = null
    private var playerId: String? = null
    private var queue: RequestQueue? = null
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        queue = RequestQueueSingleton.getInstance(applicationContext).requestQueue

        binding.btnLogin.setOnClickListener { login() }
    }

    private fun login() {
        if (!validate()) {
            return
        }
        val employeeId = binding.inputSrCode.text.toString().replace(" ", "");
        val password = binding.inputPassword.text.toString()

        pd = ProgressDialog(this)
        pd!!.setMessage("Authenticating...")
        pd!!.show()
        val request: StringRequest = object : StringRequest(
            Method.POST, Api.loginurl,
            Response.Listener { response ->
                try {
                    AppLogger.log("LOGIN DATA:: $response")
                    val responseData = JSONObject(response)
                    if (responseData.has("token")) {
                        token = responseData.getString("token")
                        val userObj = responseData.getJSONObject("user")

                        sharePrefUtils.saveString(Api.EMAIL, userObj.getString("email"))
                        sharePrefUtils.saveString(Api.NAME, userObj.getString("user_name"))
                        sharePrefUtils.saveString(Api.USER_ID, userObj.getString("id"))
                        sharePrefUtils.saveString(Api.USER_TYPE, userObj.getString("designation"))
                        sharePrefUtils.saveString(Api.PHONE, userObj.getString("phone"))
                        sharePrefUtils.saveString(
                            Api.TERRITORY_ID,
                            userObj.getString("territory_id")
                        )
                        sharePrefUtils.saveString(Api.EMPLOYEE_ID, userObj.getString("employee_id"))
                        sharePrefUtils.saveString(Api.TOKEN, token!!)
                        if (userObj.has("group_name") && !userObj.isNull("group_name")) {
                            sharePrefUtils.saveString(
                                Api.TRACE_GROUP_NAME,
                                userObj.getString("group_name")
                            )
                        }
                        if (userObj.has("group_id") && !userObj.isNull("group_id")) {
                            sharePrefUtils.saveString(
                                Api.TRACE_GROUP_ID,
                                userObj.getString("group_id")
                            )
                        }

                        var email = "ff"
                        if (userObj.has("email") && !userObj.isNull("email")) {
                            email = userObj.getString("email")
                        }
                        val phone =
                            if (userObj.getString("phone").length == 10) "0${userObj.getString("phone")}" else userObj.getString(
                                "phone"
                            )

                        // TODO: CANT SAVE THE LOG
                        BarikoiTrace.setOrCreateUser(
                            userObj.getString("user_name"),
                            email,
                            phone,
                            object : BarikoiTraceUserCallback {
                                override fun onFailure(barikoiError: BarikoiTraceError) {
                                    Log.d(
                                        "BarikoiTrace",
                                        "User created onFailure: ${barikoiError.message}"
                                    )
                                }

                                override fun onSuccess(traceUser: BarikoiTraceUser) {
                                    sharePrefUtils.saveString(Api.TRACE_USER_ID, traceUser.userId)
                                    Log.d("BarikoiTrace", "User created: $traceUser")
                                }
                            })

                        SentryAndroid.init(this) { options: SentryAndroidOptions ->
                            // Add a callback that will be used before the event is sent to Sentry.
                            // With this callback, you can modify the event or, when returning null, also discard the event.
                            options.beforeSend =
                                SentryOptions.BeforeSendCallback { event: SentryEvent, hint: Any? ->
                                    val userSentry = User()
                                    userSentry.id = userObj.getString("employee_id")
                                    userSentry.email = employeeId
                                    userSentry.username = userObj.getString("user_name")
                                    event.user = userSentry
                                    event
                                }
                        }

                        pd!!.dismiss()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else if (responseData.has("message")) {
                        pd!!.dismiss()
                        showDialog(responseData.getString("message"))
                    }
                } catch (e: JSONException) {
                    pd!!.dismiss()
                    Sentry.captureException(e)
                } catch (e: Exception) {
                    pd!!.dismiss()
                    Sentry.captureException(e)
                }
            },
            Response.ErrorListener { error ->
                pd!!.dismiss()
                Toast.makeText(this, "ERROR: $error", Toast.LENGTH_LONG).show()
                if (error is NoConnectionError) {
                    showDialog("Login failed,check your internet connection and try again")
                }
                if (error?.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Verify", "message: $s")
                        val data = JSONObject(s)
                        Log.d("Verify", "message: " + data.getString("message"))
                        showDialog("" + data.getString("message"))
                        throw Exception(data.getString("message"))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Sentry.captureException(e)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Sentry.captureException(e)
                    }
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val parameters: MutableMap<String, String> = HashMap()
                parameters["Accept"] = "application/json"
                return parameters
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val parameters: MutableMap<String, String> = HashMap()
                parameters["employee_id"] = employeeId
                parameters["password"] = password
                return parameters
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            30 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue?.add(request)
    }

    fun showDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.cancel()
            }
        val alert = builder.create()
        alert.show()

    }

    override fun onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true)
    }

    fun onLoginFailed() {
        showDialog("Login failed,check if SR Code and password is correct")
    }

    private fun validate(): Boolean {
        var valid = true
        val email = binding.inputSrCode.text.toString().replace(" ", "");
        if (email.isEmpty()) {
            binding.inputSrCode.error = "Enter a valid SR code"
            valid = false
        } else {
            binding.inputSrCode.error = null
        }
        if (binding.inputPassword.text.toString().length < 6) {
            binding.inputPassword.error = "Enter a valid password (minimum 6 characters)"
            valid = false
        } else {
            binding.inputPassword.error = null
        }
        return valid
    }
}