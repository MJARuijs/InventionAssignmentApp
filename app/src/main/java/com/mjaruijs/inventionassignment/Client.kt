package com.mjaruijs.inventionassignment

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class Client(address: String, port: Int) {

    private val channel = SocketChannel.open(InetSocketAddress(address, port))

    /**
     * Write a message to the channel.
     */
    private fun write(bytes: ByteArray) {
        val size = bytes.size + 4

        val sizeArray = ByteArray(4)
        sizeArray[0] = size.toByte()
        sizeArray[1] = (size shr 8).toByte()
        sizeArray[2] = (size shr 16).toByte()
        sizeArray[3] = (size shr 24).toByte()

        val sizeBuffer = ByteBuffer.allocate(size)
        sizeBuffer.put(sizeArray)
        sizeBuffer.put(bytes)
        sizeBuffer.rewind()

        channel.write(sizeBuffer)
    }

    /**
     * Use the above method to write a String to the channel.
     */
    fun write(string: String) = write(string.toByteArray())

    /**
     * Close the channel.
     */
    fun close() {
        channel.close()
    }
}