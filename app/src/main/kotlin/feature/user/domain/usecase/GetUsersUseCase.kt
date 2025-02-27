package feature.user.domain.usecase

import feature.user.domain.entity.User
import feature.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

interface GetUsersUseCase : () -> Flow<Result<List<User>>>

class GetUsersUseCaseImpl(private val userRepository: UserRepository) :
    GetUsersUseCase {

    override fun invoke(): Flow<Result<List<User>>> = userRepository.getUsers()
}
