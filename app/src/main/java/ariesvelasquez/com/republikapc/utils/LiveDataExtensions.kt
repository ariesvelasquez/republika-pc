package ariesvelasquez.com.republikapc.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.lang.Exception

/**
 * Syntactic sugar for [LiveData.observe] function where the [Observer] is the last parameter.
 * Hence can be passed outside the function parenthesis.
 */
inline fun <T> LiveData<T>.observe(owner: LifecycleOwner, crossinline observer: (T) -> Unit) {
    this.observe(owner, Observer { it?.apply(observer) })
}

inline fun <T>LiveData<Result<T>>.observeResult(
    owner: LifecycleOwner,
    crossinline onSuccess: (T) -> Unit,
) {
    this.observe(owner, Observer {
        it.onSuccess { content -> content.apply(onSuccess) }
    })
}

//inline fun <T>LiveData<Result<T>>.observeResult(
//    owner: LifecycleOwner,
//    crossinline onSuccess: (T) -> Unit,
//    crossinline onFailure: (t: Throwable) -> Unit
//) {
//    this.observe(owner, Observer {
//        it.onSuccess { content -> onSuccess(content) }
//            .onFailure { t -> onFailure.invoke(t) }
//    })
//}

