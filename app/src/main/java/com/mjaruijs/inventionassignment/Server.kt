package com.mjaruijs.inventionassignment

import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel

class Server(port: Int, private val onRead: (String) -> Unit) {

    private val channel = ServerSocketChannel.open()!!

    init {
        val address = InetSocketAddress(port)
        channel.bind(address)
    }

    fun start() {


        while (true) {
            val clientChannel = channel.accept()
            val channelString = clientChannel.toString()

            val startIndex = channelString.lastIndexOf('/') + 1
            val endIndex = channelString.lastIndexOf(':')

            println(channelString.substring(startIndex, endIndex))
            val sizeBuffer = ByteBuffer.allocate(4)

            clientChannel.read(sizeBuffer)
            sizeBuffer.rewind()

//            var sizeString = ""
//
//            for (i in 0 until 4) {
//                sizeString += sizeBuffer.get().toChar()
//            }

            val size = sizeBuffer.int
            println(size)

            val dataBuffer = ByteBuffer.allocate(size)
            val dataBytes = clientChannel.read(dataBuffer)

            if (dataBytes == -1) {
                throw Exception("Client was closed!")
            }
            val message = String(dataBuffer.array())
            if (message.isNotEmpty()) {
                println("RECEIVED ${String(dataBuffer.array())}")
                onRead(String(dataBuffer.array()))
            }

        }
    }


}