package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks

@SpringBootApplication
class CrdtApplication

fun main(args: Array<String>) {
	Hooks.onOperatorDebug()
	runApplication<CrdtApplication>(*args)
}
