package com.lepu.nordicble.socket

import android.os.Handler
import com.blankj.utilcode.util.LogUtils
import com.lepu.nordicble.socket.objs.SocketMsgConst
import com.lepu.nordicble.vals.hostNeedConnect
import java.io.*
import java.lang.Exception
import java.net.Socket

class SocketThread : Thread() {

    private lateinit var handler: Handler
    private lateinit var url: String
    private var port = 0
    private var socket: Socket? = null
    private var connected = false
    private var working = true
    /**
     * 接收服务器消息
     * 输入流
     */
    private lateinit var input: InputStream

    /**
     * 发送消息
     * 输出流
     */
    private lateinit var output: OutputStream



    fun setUrl(url: String, port: Int) {
        this.url = url
        this.port = port
    }

    fun setHandler(handler: Handler) {
        this.handler = handler
    }

    fun sendMessage(bytes: ByteArray) {
//        LogUtils.d(bytes.toHex())
//        writer?.println(bytes)
//        LogToFile.d("Socket", "try send msg: ${bytes.toHex()}")
        if (!connected) {
            return
        }
        try {
            output.write(bytes)
            output.flush()
        } catch (e: Exception) {
            close()
            e.printStackTrace()
        }

//        catch (e: IOException) {
//            close()
//            e.printStackTrace()
//        }
    }

    override fun run() {
        super.run()

        hostNeedConnect = false

        try {
            socket = Socket(url, port)

            connected = true
            LogUtils.d("current thread id: ${currentThread().id}")
            handler.sendMessage(handler.obtainMessage(SocketMsgConst.MSG_CONNECT, true))

            input = socket!!.getInputStream()

            output = socket!!.getOutputStream()

            while (working) {
//                val response = bufferedReader.readLine()
                val bytes = ByteArray(1024)
                val s = input.read(bytes)
                if (s > 0) {
                    val res = bytes.copyOfRange(0, s)

                    handler.sendMessage(handler.obtainMessage(SocketMsgConst.MSG_RESPONSE, res))
                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
            close()
        }

    }


    private fun close() {
        connected = false

        hostNeedConnect = true

        try {
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            input.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        handler.sendMessage(handler.obtainMessage(SocketMsgConst.MSG_CONNECT, false))
        working = false
    }

}