package base.storage.common.cashe

data class CachedEntry<E>(
    val entry: E,
    val createTime: Long
)