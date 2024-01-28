package com.barikoi.cnlapp.socket

import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpUserAuthenticator

class PusherClient {
   private val pusher: Pusher

    init {
        val pusherOption = PusherOptions()
            .setHost("backend.barikoi.com")
            .setWsPort(6001)
            .setWssPort(6002)
            .setCluster("ap2")
            .setUseTLS(true)
            .setUserAuthenticator(HttpUserAuthenticator(""))


        pusher = Pusher("myKey", pusherOption)
    }

    fun connect() {
        pusher.connect(
            object : ConnectionEventListener {
                override fun onConnectionStateChange(change: ConnectionStateChange?) {
                    println("State changed from ${change?.previousState} to ${change?.currentState}")
                }

                override fun onError(message: String?, code: String?, e: Exception?) {
                    println("There was a problem connecting! code ($code), message ($message), exception ($e)")
                }
            }, ConnectionState.ALL
        )
    }
}