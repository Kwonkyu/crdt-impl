package com.example.demo.handler

import com.example.demo.domain.LocalCounter
import com.example.demo.infra.CounterSessionManager
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * 로컬에서 사용자가 카운터를 증가시키고 그 상태를 전파시킬 때 받아서 전달하는 핸들러.
 */
@Component
class CounterPropagateHandler : WebSocketHandler {

    private val logger = KotlinLogging.logger { }

    override fun handle(session: WebSocketSession): Mono<Void?> {
        logger.info { "handling session ${session.id}" }

        // (ChatGPT) 메시지를 한 번만 받고 말 것이 아니기 때문에 toMono() 를 사용하지 않는다.
        // 이 핸들러는 Reactive 핸들러임. Flux 를 사용하여 여러 메시지를 처리할 수 있다.
        val receiveStream = session.receive().map {
            // parse payload
            val payload = it.payloadAsText.split("|").map { it.toInt() }.toIntArray()
            logger.info { "payload: ${payload.contentToString()}" }
            LocalCounter(payload, session.id)
        }.doOnNext { localCounter ->
            // state merge
            with(CounterSessionManager.COUNTER) {
                obj = obj.merge(localCounter)
            }
            logger.info { "object merged." }
            logger.info { "obj status: ${CounterSessionManager.COUNTER.obj}" }
        }

        // broadcast
        val broadcastStream = Flux
            .from(receiveStream)
            .doOnNext { localCounter -> logger.info { "broadcasting localCounter: $localCounter" } }
            .flatMap {
                Flux.fromIterable(CounterSessionManager.SESSION.sessions.values)
                    .doOnNext { logger.info { "broadcast target: ${it.id} -> open: ${it.isOpen}" } }
                    .filter { it.isOpen }
                    .flatMap { session ->
                        logger.info { "Sending counter to session ${session.id}" }
                        val counter = CounterSessionManager.COUNTER.getByteArray()
                        val binaryStream = session.binaryMessage { it.wrap(counter) }
                        session.send(Mono.just(binaryStream))
                    }
            }
        /**
         * (ChatGPT) flatMap 은 입력 값을 변환한 후, 반환된 Publisher(Mono 또는 Flux)를 구독하고 그 결과를 스트림에 병합합니다.
         * 즉, 비동기 작업을 실행하고 그 결과를 스트림에 포함시킵니다.
         * 만약 flatMap 을 사용하면, 각 session.send(...) 작업이 즉시 실행되고 결과가 병합됩니다.
         */

//        return receiveStream.thenMany(broadcastStream).then()
//        return Flux.concat(receiveStream, broadcastStream).then()
        return broadcastStream.then()
    }
}