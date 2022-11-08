package com.barikoi.cnlapp.Utils.ApiService

import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Utils.VolleyMultipartRequest
import java.util.ArrayList

object ApiServices {

    fun apiPOST(url: String, queue: RequestQueue, token: String, parameters: MutableMap<String, String> , mListener: ApiServiceListener){
        val request = object : StringRequest(Request.Method.POST, url,
            {
                    response ->
                try{
                    mListener.onResponseSuccess(response)
                }catch (e: Exception){
                    mListener.onException(e)
                }
            },
            {
                error ->
                mListener.onResponseFailure(error)
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val parameters: MutableMap<String, String> = HashMap()
                parameters["Accept"] = "application/json"
                if (token != "") {
                    parameters["Authorization"] = "bearer $token"
                }
                return parameters
            }
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                return parameters
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            40 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }

    fun apiPOSTMultipart(url: String, queue: RequestQueue, token: String, parameters: MutableMap<String, String>, byteparams: MutableMap<String, VolleyMultipartRequest.DataPart>, mListener: ApiServiceListener){
        val request = object : VolleyMultipartRequest(Request.Method.POST, url,
            {
                    response ->
                try{
                    mListener.onNetworkResponseSuccess(response)
                }catch (e: Exception){
                    mListener.onException(e)
                }
            },
            {
                    error ->
                mListener.onResponseFailure(error)
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val parameters: MutableMap<String, String> = HashMap()
                parameters["Accept"] = "application/json"
                if (token != "") {
                    parameters["Authorization"] = "bearer $token"
                }
                return parameters
            }
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                return parameters
            }

            override fun getByteData(): Map<String, DataPart> {
                return byteparams
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }

    fun apiGET(url: String, queue: RequestQueue, token: String, mListener: ApiServiceListener){
        val request = object : StringRequest(Request.Method.GET, url,
            {
                    response ->
                try{
                    mListener.onResponseSuccess(response)
                }catch (e: Exception){
                    mListener.onException(e)
                }
            },
            {
                    error ->
                mListener.onResponseFailure(error)
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val parameters: MutableMap<String, String> = HashMap()
                parameters["Accept"] = "application/json"
                if (token != "") {
                    parameters["Authorization"] = "bearer $token"
                }
                return parameters
            }
            /*@Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                return parameters
            }*/
        }
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }
}