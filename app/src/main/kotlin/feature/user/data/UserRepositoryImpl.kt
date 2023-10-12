package feature.user.data

import base.storage.common.cashe.CachePolicy
import base.storage.common.storage.LocalStorage
import feature.user.data.dto.ResultDto
import feature.user.data.mapper.toDomain
import feature.user.data.service.UserApi
import feature.user.domain.entity.User
import feature.user.domain.entity.UserId
import feature.user.domain.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.seconds

class UserRepositoryImpl(private val httpClient: HttpClient) : UserRepository {

    private val usersLocalStorage = LocalStorage<Unit, List<User>>(
        maxElements = 1,
        cachePolicy = CachePolicy(10.seconds),
        network = {
            httpClient.get {
                url(UserApi.GET_USERS)
            }.body<ResultDto>().users.map { it.toDomain() }
        },
    )

    override fun getUser(userId: UserId): Flow<Result<User>> = usersLocalStorage.get(Unit)
        .map { result ->
            result.map { list -> list.find { item -> item.id == userId }!! }
        }

    override fun getUsers(): Flow<Result<List<User>>> = usersLocalStorage.get(Unit)

    override suspend fun deleteUser(userId: UserId) {
        usersLocalStorage.update(Unit) { list ->
            list.filter { user -> user.id != userId }
        }
    }

    override suspend fun updateUser(user: User) {
        usersLocalStorage.update(Unit) { list ->
            list.map { item -> if (item.id == user.id) user else item }
        }
    }
}
