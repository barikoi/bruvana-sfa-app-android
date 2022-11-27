package com.barikoi.cnlapp.Activity

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.protocol.User
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class LoginActivity : AppCompatActivity() {

    //var etPhoneNumber: EditText? = null
    var etSRCode: EditText? = null
    var etPassword: EditText? = null
    var signin_link: TextView? = null
    var resetpass:TextView? = null
    var login: Button? = null
    var skip: Button? = null
    private var loginSuccess: Boolean? = false
    var pd: ProgressDialog? = null
    private var player_id: String? = null
    private var queue: RequestQueue? = null
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    private fun init() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
        etSRCode = findViewById<View>(R.id.input_sr_code) as EditText
        etPassword = findViewById<View>(R.id.input_password) as EditText
        //resetpass = findViewById<View>(R.id.textView_resetpass) as TextView
        //signin_link = findViewById<View>(R.id.link_signup) as TextView
        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()

        login = findViewById<View>(R.id.btn_login) as Button
        login?.setOnClickListener(View.OnClickListener { login() })
        /*signin_link?.setOnClickListener(View.OnClickListener {
            val signup = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(signup)
            finish()
        })*/

        /*resetpass!!.setOnClickListener {
            NetworkUtils.showresetpassInputDialog(this@LoginActivity)
        }*/
    }

    private fun login() {
        if (!validate()) {
            //onLoginFailed()
            return
        }
        val email = etSRCode!!.text.toString().replace(" ", "");
        val password = etPassword!!.text.toString()
        pd = ProgressDialog(this)
        pd!!.setMessage("Authenticating...")
        pd!!.show()
        val queue = RequestQueueSingleton.getInstance(applicationContext).requestQueue
        val request: StringRequest = object : StringRequest(
            Method.POST, Api.loginurl,
            Response.Listener { response ->
                try {
                    val responsedata = JSONObject(response)
                    if(responsedata.has("token")) {
                        token = responsedata.getString("token")
                        val userObj = responsedata.getJSONObject("user")
                        val prefs = PreferenceManager.getDefaultSharedPreferences(
                            applicationContext
                        )
                        val editor = prefs.edit()
                        editor.putString(Api.EMAIL, userObj.getString("email"))
                        editor.putString(Api.NAME, userObj.getString("name"))
                        editor.putString(Api.USER_ID, userObj.getString("id"))
                        editor.putString(Api.USER_TYPE, userObj.getString("designation"))
                        editor.putString(Api.PHONE, userObj.getString("phone"))
                        editor.putString(Api.SR_CODE, userObj.getString("sr_code"))
                        editor.putString(Api.EMPLOYEE_ID, userObj.getString("employee_id"))
                        editor.putString(Api.TOKEN, token)
                        editor.commit()

                        SentryAndroid.init(this) { options: SentryAndroidOptions ->
                            // Add a callback that will be used before the event is sent to Sentry.
                            // With this callback, you can modify the event or, when returning null, also discard the event.
                            options.beforeSend =
                                SentryOptions.BeforeSendCallback { event: SentryEvent, hint: Any? ->
                                    val userSentry = User()
                                    userSentry.id = userObj.getString("sr_code")
                                    userSentry.email = email
                                    userSentry.username = userObj.getString("name")
                                    event.user = userSentry
                                    event
                                }
                        }
                        routeToAppropriatePage(2)
                        //OneSignal.setEmail(email);
                        pd!!.dismiss()
                    }else if(responsedata.has("message")){
                        pd!!.dismiss()
                        showDialog( responsedata.getString("message"))
                    }

                    // onLoginFailed();
                } catch (e: JSONException) {
                    pd!!.dismiss()
                    Sentry.captureException(e)
                    Toast.makeText(applicationContext,"Error" +e.message, Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener { error ->
                pd!!.dismiss()
                //NetworkcallUtils.handleResponse(error, getApplicationContext());
                if (error is NoConnectionError) {
                    showDialog("Login failed,check your internet connection and try again")
                }
                if (error != null && error.networkResponse != null) {
                    try {
                        //NetworkResponse response = error.networkResponse;
                        val s = String(error.networkResponse.data)
                        Log.d("Verify", "message: $s")
                        val data = JSONObject(s)
                        Log.d("Verify", "message: " + data.getString("message"))
                        showDialog(""+data.getString("message"))
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
            override fun getParams(): Map<String, String>? {
                val parameters: MutableMap<String, String> = HashMap()
                //parameters.put("id", id.getText().toString());
                Log.d("MainActivity", "login email: $email")
                //parameters.put("device_ID",player_id);
                parameters["sr_code"] = email
                parameters["password"] = password
                return parameters
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            30 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }

    fun showDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })
        val alert = builder.create()
        alert.show()

    }

    override fun onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true)
    }

    fun onLoginFailed() {
        showDialog("Login failed,check if SR Code and password is correct",)

    }

    private fun validate(): Boolean {
        var valid = true
        /*val phoneNumber = etPhoneNumber!!.text.toString()
        if (phoneNumber.isEmpty()) {
            etPhoneNumber!!.error = "Enter a valid phone number"
            valid = false
        } else {
            etPhoneNumber!!.requestFocus()
            etPhoneNumber!!.error = null
        }*/

        val email = etSRCode!!.text.toString().replace(" ", "");
        if (email.isEmpty()) {
            etSRCode!!.setError("Enter a valid SR code")
            valid = false
        } else {
            etSRCode!!.setError(null)
        }
        if (etPassword!!.text.toString().length < 6) {
            etPassword!!.error = "Enter a valid password (minimum 6 characters)"
            valid = false
        } else {
            etPassword!!.error = null
        }
        return valid
    }

    private fun routeToAppropriatePage(routeopt: Int) {
        // Example routing
        when (routeopt) {
            0 -> {
                /*val i = Intent(this, SignupActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
                finish()*/
            }
            1 -> {
                val intent = intent
                startActivity(intent)
            }
            2 -> {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                finish()
            }
        }
    }
}