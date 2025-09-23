package com.example.calculator.location

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

val LOG_TAG: String = "MY_CLIENT"

class my_client {
    private lateinit var socket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter
    @Volatile private var connected = false

    fun start(host: String, port: Int) {
        Thread {
            socket = Socket(host, port)

            if(socket == null){
                Log.d(LOG_TAG, "ERROR in socket create")
                kotlin.system.exitProcess(1)
            }

            reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            if(reader == null){
                Log.d(LOG_TAG, "ERROR in reader create")
                kotlin.system.exitProcess(1)
            }

            writer = PrintWriter(socket.getOutputStream(), true)

            if (writer == null) {
                Log.d(LOG_TAG, "ERROR in writer create")
                kotlin.system.exitProcess(1)
            }

            connected = true

        }.start()
    }

    fun send(message: String) {
        if (connected) {
            Thread { writer.println(message) }.start()
        }
    }

    fun stop() {
        reader.close()
        writer.close()
        socket.close()
    }
}
