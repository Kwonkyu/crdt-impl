package com.example.demo.handler

import com.example.demo.infra.CounterSessionManager
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * UUID 를 기반으로 Counter Lobby 에 참여하는 핸들러.
 */
@Component
class CounterLobbyHandler: WebSocketHandler {

    private val logger = KotlinLogging.logger {  }

    override fun handle(session: WebSocketSession): Mono<Void?> {
        // store session for broadcasting
        if (CounterSessionManager.SESSION.getSession(session.id) == null) {
            logger.info { "added session ${session.id} to system." }
            CounterSessionManager.SESSION.addSession(session.id, session)
        }

        val giveCurrentCounter = session.binaryMessage { factory -> factory.wrap(CounterSessionManager.COUNTER.getByteArray()) }
        val counterStream = Mono.just(giveCurrentCounter)
        // (ChatGPT) If Mono is sent when it's completed Spring WebSocket closes the session.
        // Use Flux.concat to make it Flux and keep the session open.

//        val checkStream = Flux
//            .interval(java.time.Duration.ofSeconds(1))
        /**
         * (ChatGPT) session.send(sendStream)은 WebSocket 세션이 닫히면 자동으로 중단됩니다.
         * 즉, 세션이 닫히는 시점에서 더 이상 메시지가 전송되지 않습니다.
         * 따라서 sendStream에서 pingMessage를 계속 보내는 작업도 세션 종료 시 중단됩니다.
         * 하지만, sendStream 내부에서 명시적으로 pingMessage 전송을 중단하고 싶다면, 세션 종료 신호를 감지하여
         * 스트림을 종료하는 추가적인 처리가 필요할 수 있습니다. 예를 들어, takeUntilOther를 사용하여 세션 종료 신호를 감지하고
         * 스트림을 종료할 수 있습니다. 현재 코드에서는 session.receive()의 doOnComplete와 doFinally 블록이
         * 세션 종료를 처리하고 있으므로, 추가적인 처리가 없어도 세션이 닫히면 pingMessage 전송이 중단됩니다.
         * 따라서, takeUntilOther는 선택 사항이며, 특정 요구 사항에 따라 추가할 수 있습니다.
         */
//            .takeUntilOther(session.closeStatus())
//            .map { session.pingMessage { it.wrap(ByteArray(0)) }
//        }
        val checkStream = session.receive()
            .doOnComplete { logger.info { "Client ${session.id} closed the connection." } }
            .map { session.pingMessage { it.wrap(ByteArray(0)) } }
        val sendStream = Flux.concat(counterStream, checkStream)
        return session.send(sendStream).doFinally {
            logger.info { "session ${session.id} closed." }
            CounterSessionManager.SESSION.removeSession(session.id)
        }
    }
}