package com.mjaruijs.inventionassignment

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class Client(private val channel: SocketChannel) {

    constructor(address: String, port: Int) : this(SocketChannel.open(InetSocketAddress(address, port))) {
//        println("HALLLLLO")
    }

    private fun write(bytes: ByteArray) {

        val size = bytes.size + 4

        val sizeArray = ByteArray(4)
        sizeArray[0] = size.toByte()
        sizeArray[1] = (size shr 8).toByte()
        sizeArray[2] = (size shr 16).toByte()
        sizeArray[3] = (size shr 24).toByte()

//        println(String(bytes).length)
//        println("SENDING")
//        println(size)
//        println(String(bytes))

        val sizeBuffer = ByteBuffer.allocate(size)
        sizeBuffer.put(sizeArray)
        sizeBuffer.put(bytes)
        sizeBuffer.rewind()

        channel.write(sizeBuffer)
    }

    fun write(string: String) = write(string.toByteArray())

    fun close() {
        channel.close()
    }
}