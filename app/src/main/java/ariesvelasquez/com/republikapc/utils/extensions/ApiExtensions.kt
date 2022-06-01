package ariesvelasquez.com.republikapc.utils.extensions

import ariesvelasquez.com.republikapc.network.Resource2
import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = { Unit },
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow<Resource2<ResultType>> {
    emit(Resource2.Loading(null))
    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(Resource2.Loading(data))

        try {
            saveFetchResult(fetch())
            query().map { Resource2.Success(it) }
        } catch (throwable: Throwable) {
            onFetchFailed(throwable)
            query().map { Resource2.Error(throwable, it) }
        }
    } else {
        query().map { Resource2.Success(it) }
    }

    emitAll(flow)
}