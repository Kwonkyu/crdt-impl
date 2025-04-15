package com.example.demo.handler.http

import com.example.demo.infra.CounterSessionManager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/counter")
class DebugHandler {

    @PostMapping
    fun increaseCounter(): ResponseEntity<String> {
         CounterSessionManager.COUNTER.obj.values[0] += 100 // Increment the counter at position 0
         return ResponseEntity.ok("Counter increased")
    }

    @GetMapping
    fun getCounter(): ResponseEntity<Any> {
        return ResponseEntity.ok(CounterSessionManager.COUNTER.getByteArray())
    }
}