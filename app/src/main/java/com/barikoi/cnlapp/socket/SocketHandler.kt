package com.barikoi.cnlapp.socket

import com.barikoi.cnlapp.utils.AppLogger
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {

    private lateinit var mSocket: Socket

    @Synchronized
    fun setSocket(token: String) {
        AppLogger.log("TOKEN: $token")
        try {
            mSocket =
                IO.socket("http://tracev2.barikoimaps.dev?authorization=Bearer+${token}")
        } catch (e: URISyntaxException) {
            AppLogger.log("SocketHandler:: $e")
        } catch (e: Exception) {
            AppLogger.log("SocketHandler:: $e")

        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()

        if (mSocket.connected()) {
            AppLogger.log("SOCKET : $mSocket")
        }
    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }
}