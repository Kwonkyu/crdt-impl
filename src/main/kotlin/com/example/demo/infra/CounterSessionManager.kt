package com.example.demo.infra

import com.example.demo.domain.LocalCounter
import org.springframework.web.reactive.socket.WebSocketSession

/**
 * 현재 애플리케이션에 연결된 세션을 관리하고 counter state 를 전파할 수 있도록 세션을 조회하여 제공할 수 있는 클래스.
 */
class CounterSessionManager {

    object SESSION {
        val sessions = mutableMapOf<String, WebSocketSession>()

        fun addSession(sessionId: String, session: WebSocketSession) {
            sessions[sessionId] = session
        }

        fun removeSession(sessionId: String) {
            sessions.remove(sessionId)
        }

        fun getSession(sessionId: String): WebSocketSession? {
            return sessions[sessionId]
        }
    }

    object COUNTER {
        // center of truth
        var obj = LocalCounter(IntArray(10), "system")

        fun getByteArray(): ByteArray {
            val byteArray = ByteArray(10 * Int.SIZE_BYTES)
            for (i in obj.values.indices) {
                val byte1 = obj.values[i].and(0xFF).toByte()
                val byte2 = obj.values[i].ushr(8).and(0xFF).toByte()
                val byte3 = obj.values[i].ushr(16).and(0xFF).toByte()
                val byte4 = obj.values[i].ushr(24).and(0xFF).toByte()
                System.arraycopy(byteArrayOf(byte4, byte3, byte2, byte1), 0, byteArray, i * Int.SIZE_BYTES, Int.SIZE_BYTES)
            }
            return byteArray
        }
    }
}