package com.example.demo

import com.example.demo.handler.CounterLobbyHandler
import com.example.demo.handler.CounterPropagateHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Configuration
class WebSocketConfig {

     @Bean
     fun webSocketHandlerMapping(
            counterLobbyHandler: CounterLobbyHandler,
            counterPropagateHandler: CounterPropagateHandler
     ): HandlerMapping {
         val mappings = mapOf(
             "/ws/counter/join" to counterLobbyHandler,
             "/ws/counter/propagate" to counterPropagateHandler
         )
         return SimpleUrlHandlerMapping(mappings, -1)
     }
}