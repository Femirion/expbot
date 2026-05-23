package com.femirion.expbot.expbot.service

import com.femirion.expbot.expbot.domain.entity.User
import com.femirion.expbot.expbot.`in`.provider.UserProvider
import org.springframework.stereotype.Service

@Service
class UserService(
    val userProvider: UserProvider
) {

    fun getUser(id: Long): User? {
        return userProvider.getUserById(id)
    }

    fun createUser(user: User): User {
        return userProvider.createUser(user)
    }


}