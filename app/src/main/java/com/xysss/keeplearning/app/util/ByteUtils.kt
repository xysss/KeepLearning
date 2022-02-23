package com.xysss.keeplearning.app.util

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.xor


object ByteUtils {

    const val FRAME55: Byte = 0x55
    const val FRAME23: Byte = 0x23
    const val FRAMEFF: Byte = 0xFF.toByte()
    const val FRAME00: Byte = 0x00
    const val FRAME01:Byte=0x01

    const val Msg80: Byte = 0x80.toByte()
    const val Msg90: Byte = 0x90.toByte()
    const val Msg81: Byte = 0x81.toByte()
    const val MsgA1: Byte = 0xA1.toByte()
    lateinit var afterBytes: ByteArray
    private val dealBytesList = ArrayList<Byte>()

    fun cal(second: Int): String {
        var h = 0
        var d = 0
        var s = 0
        val temp = second % 3600
        if (second > 3600) {
            h = second / 3600
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60
                    if (temp % 60 != 0) {
                        s = temp % 60
                    }
                } else {
                    s = temp
                }
            }
        } else {
            d = second / 60
            if (second % 60 != 0) {
                s = second % 60
            }
        }
        return h.toString() + "时" + d + "分" + s + "秒"
    }


    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    fun hexStringToBytes(hexString: String): ByteArray {
        val mHexString = hexString.uppercase(Locale.getDefault())
        val length = mHexString.length / 2
        val hexChars = mHexString.toCharArray()
        val byteArrayResult = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            byteArrayResult[i] = (charToByte(hexChars[pos]).toInt().shl(4) or charToByte(hexChars[pos + 1]).toInt()).toByte()
        }
        Log.d("TAG", "hexStringToBytes: "+ byteArrayResult.contentToString())
        return byteArrayResult
    }

    fun getNoMoreThanTwoDigits(number: Float): String {
        val format = DecimalFormat("0.###")
        //未保留小数的舍弃规则，RoundingMode.FLOOR表示直接舍弃。
        format.roundingMode = RoundingMode.FLOOR
        return format.format(number)
    }

    fun getDateTime(s: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            val netDate = Date(s.toLong() * 1000)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    /**
     * Convert byte[] to string
     */
    fun bytesToHexString(src: ByteArray?): String? {
        val stringBuilder = StringBuilder("")
        if (src == null || src.isEmpty()) {
            return null
        }
        for (element in src) {
            val v = element.toInt() and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }


    fun revercRevCode(bytes: ArrayList<Byte>?): ByteArray? {
        dealBytesList.clear()
        bytes?.let {
            var i = 0
            while (i < it.size) {
                if (it[i] == FRAMEFF) {
                    if (it[i + 1] == FRAMEFF) {
                        dealBytesList.add(FRAMEFF)
                        i++
                    } else if (it[i + 1] == FRAME00) {
                        dealBytesList.add(FRAME55)
                        i++
                    } else {
                        dealBytesList.add(it[i])
                    }
                } else {
                    dealBytesList.add(it[i])
                }
                i++
            }
        }

        dealBytesList.let {
            afterBytes = ByteArray(it.size)
            for (i in afterBytes.indices) {
                afterBytes[i] = it[i]
            }
        }

        if (afterBytes[afterBytes.size - 1] == FRAME23 && afterBytes[0] == FRAME55) {
            //CRC校验
            //val crc16Str = getCrc16Str(tempBytes)
            dealBytesList.clear()
            return afterBytes
        }
        return null
    }


    fun byteToHexString(src: Byte): String = Integer.toHexString((src.toInt() and 0xFF))

    /**
     * 亦或校验(BCC校验)
     *
     * @param datas
     * @return
     */
    fun getBCCResult(datas: ByteArray): Byte {
        var temp = datas[0]
        for (i in 1 until datas.size) {
            temp = temp xor datas[i]
        }
        return temp
    }

    fun getBCCResult(hexString: String): String {
        val byteToHexString = byteToHexString(getBCCResult(hexStringToBytes(hexString)))
        return if (byteToHexString.length < 2)  "0$byteToHexString" else byteToHexString
    }

    fun byteArrayToHexString(byteArray: ByteArray): String {
        val sb = StringBuilder()
        for (i in byteArray.indices) {
            var hex = Integer.toHexString((byteArray[i]).toInt() and 0xFF)
            if(hex.length == 1){
                hex = "0$hex"
                sb.append(hex.uppercase(Locale.getDefault()))
            }
        }
        return sb.toString()
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private fun charToByte(c: Char): Byte = "0123456789ABCDEF".indexOf(c).toByte()

    //CRC校验
    fun getCrc16Str(arr_buff: ByteArray, little_endian: Boolean = true): ByteArray {
        val len = arr_buff.size
        // 预置 1 个 16 位的寄存器为十六进制FFFF, 称此寄存器为 CRC寄存器。
        var crc = 0xFFFF
        for (i in 0 until len) {
            crc = ((crc and 0xFF00) or (crc and 0x00FF) xor (arr_buff[i].toInt() and 0xFF))
            for (j in 0 until 8) {
                // 把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
                if ((crc and 0x0001) > 0) {
                    // 如果移出位为 1, CRC寄存器与多项式A001进行异或
                    crc = crc.shr(1)
                    crc = crc xor 0xA001
                } else {
                    // 如果移出位为 0,再次右移一位
                    crc = crc.shr(1)
                }
            }
        }
        val result = ByteArray(2)
        result[if(little_endian) 0 else 1] = (crc.shr(8) and 0xFF).toByte()
        result[if(little_endian) 1 else 0] = (crc and 0xFF).toByte()
        return result
    }

}