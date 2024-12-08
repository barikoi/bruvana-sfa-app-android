package com.barikoi.cnlapp.socket


import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.utils.AppLogger
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketHandler {

    private lateinit var mSocket: Socket

    @Synchronized
    fun setSocket(token: String) {
        AppLogger.log("Trace TOKEN: $token")
        val options = IO.Options().apply {
            reconnection = true      // Enable automatic reconnection
            reconnectionAttempts = 5  // Limit the number of reconnection attempts
            reconnectionDelay = 2000  // Delay in milliseconds before each attempt
            transports = arrayOf("websocket")
        }
        try {
            mSocket =
                IO.socket("${BuildConfig.TRACE_BASE_URL}?authorization=Bearer+${token}", options)
            mSocket.connect()
//            https://trace.bmapsbd.com?authorization=Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjp7ImNvbXBhbnkiOiI2NzFhMGI2MjhlYjQ4YjM1MzE1MGJiMzMiLCJyZXNldF9wYXNzd29yZF90b2tlbiI6bnVsbCwiX2lkIjoiNjcxYTBlNGE0NzZlY2QyYzlmYWRhODMzIiwidXNlcl9pZCI6NTA4MywibmFtZSI6IkNhcmUgTnV0cml0aW9uIiwiZW1haWwiOiJjYXJlbnV0cml0aW9uQGdtYWlsLmNvbSIsInBob25lIjoiMDEzMjQ3MjUyMTAiLCJpc19hbGxvd2VkIjowLCJ1c2VyX2xhc3RfbGF0IjoyMy44MjkxMzYwNzE3ODY1NjgsInVzZXJfbGFzdF9sb24iOjkwLjM2Mzc5NDE5MTgwNTYxLCJjcmVhdGVkX2F0IjoiMjAyNC0wMS0xNFQxNjoyODo0My4wMDBaIiwidXBkYXRlZF9hdCI6IjIwMjQtMTEtMDRUMDU6MTc6NTUuNDk2WiIsImNvbXBhbnlfaWQiOjMxLCJwb3NpdGlvbl91cGRhdGVkX2F0IjoiMjAzMi0xMi0zMVQxOTo0Njo1MC4wMDBaIiwicm9sZXMiOlt7Il9pZCI6IjY3MWEwZTRhNDc2ZWNkMmM5ZmFkYTgzNCIsInVzZXIiOiI2NzFhMGU0YTQ3NmVjZDJjOWZhZGE4MzMiLCJjb21wYW55IjoiNjcxYTBiNjI4ZWI0OGIzNTMxNTBiYjMzIiwicm9sZSI6IkFETUlOIn1dLCJ1cGRhdGVkQXQiOiIyMDI0LTExLTA0VDA1OjE3OjU1LjQ5NloiLCJfX3YiOjEsImNvbXBhbmllcyI6W10sImVtYWlsX3ZlcmlmaWNhdGlvbl90b2tlbiI6bnVsbCwidXNlcl90eXBlIjoiQURNSU4iLCJlbWFpbF92ZXJpZmllZF9hdCI6IjIwMjQtMTAtMjRUMTA6MTE6MTQuNTQ0WiJ9LCJjb21wYW55X2lkIjoiNjcxYTBiNjI4ZWI0OGIzNTMxNTBiYjMzIiwiaWF0IjoxNzMwNzgwMzY3LCJleHAiOjE3MzEzODUxNjd9.6b6-sb10UdXphraVhEEYXKyTlYmTIyVvSi8n6AqDnUQ
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
            AppLogger.log("SocketHandler:: SOCKET : $mSocket")
        } else {
            AppLogger.log("SocketHandler:: SOCKET NOT CONNECTED")
        }
    }

    fun onConnectError() {
        mSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            AppLogger.log("SocketHandler:: Connection Error: ${args[0]}")
        }.on(Socket.EVENT_DISCONNECT) {
            AppLogger.log("SocketHandler:: Reconnection failed")
        }
    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }
}