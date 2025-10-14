package com.example.calculator.location

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

val LOG_TAG: String = "MY_CLIENT"

class my_client {
    private lateinit var socket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter

    @Volatile
    private var connected = false

    fun start(host: String, port: Int) {
        Thread {
            while (!connected) {
                try {
                    socket = Socket(host, port)

                    reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                    writer = PrintWriter(socket.getOutputStream(), true)

                    connected = true

                } catch (e: IOException) {
                    Thread.sleep(4000)
                }
            }
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
