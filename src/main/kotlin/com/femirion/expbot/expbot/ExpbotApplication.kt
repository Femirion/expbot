package com.femirion.expbot.expbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExpbotApplication

fun main(args: Array<String>) {
	runApplication<ExpbotApplication>(*args)
}
