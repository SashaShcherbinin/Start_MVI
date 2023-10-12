@file:Suppress("MemberVisibilityCanBePrivate", "OPT_IN_USAGE")

package base.storage.common.storage

import base.storage.common.cashe.CachePolicy
import base.storage.common.cashe.CachedEntry
import base.storage.common.cashe.createEntry
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.Result.Companion.success

/**
 * Automatic caching values from network to memory and database(optional) by key ->value
 * @param maxElements max elements after adding making clean up for the first element
 * @param network network method request to get data from network
 * @param permanentStorage data base interface to get, insert, or clean data from data base
 * */
class LocalStorage<K : Any, E>(
    private val maxElements: Int,
    private val cachePolicy: CachePolicy,
    private val network: (suspend (K) -> E),
    private val permanentStorage: PermanentStorage<K, E>? = null
) {

    private val updateChannel = Channel<Pair<K, E>>()
    private val cache: MutableMap<K, CachedEntry<E>> = mutableMapOf()

    fun get(key: K): Flow<Result<E>> {
        val requestFlow: Flow<Result<E>> = flow {
            val cachedEntry: CachedEntry<E>? = cache[key]
            if (cachedEntry != null) {
                emit(success(cachedEntry.entry))
                if (cachePolicy.isExpired(cachedEntry.createTime)) {
                    runCatching {
                        fetchData(key)
                    }.onSuccess { emit(success(it)) }
                }
            } else {
                val permanentEntry = permanentStorage?.read(key)
                if (permanentEntry != null) {
                    addNewValue(key, permanentEntry)
                    emit(success(permanentEntry))
                    runCatching {
                        fetchData(key)
                    }.onSuccess { emit(success(it)) }
                } else {
                    val value: Result<E> = runCatching { fetchData(key) }
                    emit(value)
                }
            }
        }
        val observeFlow = updateChannel.receiveAsFlow()
            .filter { it.first == key }
            .map { success(it.second) }
        return flowOf(requestFlow, observeFlow).flattenConcat()
    }

    private fun addNewValue(key: K, permanentEntry: E) {
        if (cache.size > maxElements) {
            val oldValueKey = cache.minBy {
                it.value.createTime
            }.key
            cache.remove(oldValueKey)
        }
        cache[key] = cachePolicy.createEntry(permanentEntry)
    }

    private suspend fun fetchData(key: K): E {
        val value = network(key)
        addNewValue(key, value)
        permanentStorage?.insertOrUpdate(key, value)
        updateChannel.trySend(key to value)
        return value
    }

    suspend fun update(
        key: K,
        onUpdateCallback: (E) -> E
    ) {
        val oldEntry = cache[key]?.entry
        if (oldEntry != null) {
            updateEntity(key, onUpdateCallback(oldEntry))
        } else {
            permanentStorage?.read(key)?.let { it ->
                updateEntity(key, onUpdateCallback(it))
            }
        }
    }

    private suspend fun updateEntity(key: K, newEntry: E) {
        permanentStorage?.insertOrUpdate(key, newEntry)
        addNewValue(key, newEntry)
        updateChannel.trySend(key to newEntry)
    }

    suspend fun refresh(key: K) {
        fetchData(key)
    }
}

interface PermanentStorage<K, E> {
    suspend fun read(key: K): E?
    suspend fun remove(key: K)
    suspend fun insertOrUpdate(key: K, entity: E)
}