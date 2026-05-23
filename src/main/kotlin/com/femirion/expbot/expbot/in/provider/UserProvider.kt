package com.femirion.expbot.expbot.`in`.provider

import com.femirion.expbot.expbot.domain.entity.User
import com.femirion.expbot.expbot.`in`.mapper.UserMapper
import com.femirion.expbot.expbot.`in`.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserProvider(
    val userMapper: UserMapper,
    val userRepository: UserRepository,
) {

    fun getUserById(userId: Long): User? {
        return userRepository
            .findById(userId)
            .map { entity -> userMapper.toUser(entity) }
            .orElse(null)
    }

    fun getAll(): List<User> {
        return userRepository
            .findAll()
            .map { entity -> userMapper.toUser(entity) }
    }

    fun createUser(user: User): User {
        val savedEntity = userRepository.save(userMapper.toEntity(user))
        return userMapper.toUser(savedEntity)
    }
}