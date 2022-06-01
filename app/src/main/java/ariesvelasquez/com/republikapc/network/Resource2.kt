package ariesvelasquez.com.republikapc.network

sealed class Resource2<T> {
    abstract val data: T?

    data class Loading<T>(override val data: T? = null) : Resource2<T>()
    data class Success<T>(override val data: T? = null) : Resource2<T>()
    data class Error<T>(val throwable: Throwable, override val data: T? = null) : Resource2<T>()
}