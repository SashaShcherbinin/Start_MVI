package feature.user.domain.usecase

import feature.user.domain.entity.User
import feature.user.domain.repository.UserRepository
import kotlinx.coroutines.delay

interface UpdateUserUseCase : suspend (User) -> Unit

class UpdateUserUseCaseImpl(private val userRepository: UserRepository) : UpdateUserUseCase {

    override suspend fun invoke(user: User) {
        delay(2000)
        userRepository.updateUser(user)
    }
}
