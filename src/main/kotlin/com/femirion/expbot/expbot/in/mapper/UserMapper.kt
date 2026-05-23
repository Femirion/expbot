package com.femirion.expbot.expbot.`in`.mapper

import com.femirion.expbot.expbot.`in`.repository.UserEntity
import com.femirion.expbot.expbot.domain.entity.User
import org.springframework.stereotype.Service

@Service
class UserMapper {

    fun toEntity(user: User): UserEntity {
        return UserEntity(
            id = user.id,
            telegramUserId = user.telegramUserId,
            displayName = user.displayName,
            status = user.status,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
        )
    }

    fun toUser(userEntity: UserEntity): User {
        return User(
            id = userEntity.id,
            telegramUserId = userEntity.telegramUserId,
            displayName = userEntity.displayName,
            status = userEntity.status,
            createdAt = userEntity.createdAt,
            updatedAt = userEntity.updatedAt,
            telegramUsername = userEntity.telegramUsername ?: "",
        )
    }

}