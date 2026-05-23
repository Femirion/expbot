package com.femirion.expbot.expbot.`in`.controller

import com.femirion.expbot.expbot.domain.entity.User
import com.femirion.expbot.expbot.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    val userService: UserService,
) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable("id") id: Long): ResponseEntity<User> {
        return ResponseEntity.ok(userService.getUser(id))
    }

    @PostMapping("/register")
    fun createUser(@RequestBody user: User): ResponseEntity<User> {
        return ResponseEntity.ok(userService.createUser(user))
    }
}