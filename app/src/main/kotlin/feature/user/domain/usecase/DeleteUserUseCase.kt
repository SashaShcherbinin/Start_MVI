package feature.user.domain.usecase

import feature.user.domain.repository.UserRepository
import kotlinx.coroutines.delay

typealias UserId = String

interface DeleteUserUseCase : suspend (UserId) -> Unit

class DeleteUserUseCaseImpl(private val userRepository: UserRepository) : DeleteUserUseCase {

    override suspend fun invoke(userId: UserId) {
        delay(2000)
        userRepository.deleteUser(userId)
    }
}
