package base.storage.common.cashe

import kotlin.time.Duration

data class CachePolicy(
    private val time: Duration,
) {

    fun isExpired(createdTime: Long): Boolean {
        val currentTime = getTime()
        return currentTime - createdTime > time.inWholeMilliseconds
    }

    fun getTime(): Long {
        return System.currentTimeMillis()
    }
}

fun <T> CachePolicy.createEntry(entity: T): CachedEntry<T> {
    return CachedEntry(entry = entity, createTime = getTime())
}