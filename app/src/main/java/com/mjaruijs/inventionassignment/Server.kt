package com.mjaruijs.inventionassignment

import java.lang.Exception
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel

class Server(port: Int, private val onRead: (String) -> Unit) {

    private val channel = ServerSocketChannel.open()!!

    init {
        val address = InetSocketAddress(port)
        channel.bind(address)
    }

    /**
     * Start the server, which accepts clients. Those clients can be used as a remote control in order to start/stop measurements.
     */
    fun start() {
        while (true) {
            val clientChannel = channel.accept()
            val sizeBuffer = ByteBuffer.allocate(4)

            clientChannel.read(sizeBuffer)
            sizeBuffer.rewind()

            val size = sizeBuffer.int

            val dataBuffer = ByteBuffer.allocate(size)
            val dataBytes = clientChannel.read(dataBuffer)

            if (dataBytes == -1) {
                throw Exception("Client was closed!")
            }

            val message = String(dataBuffer.array())
            if (message.isNotEmpty()) {
                onRead(String(dataBuffer.array()))
            }
        }
    }
}