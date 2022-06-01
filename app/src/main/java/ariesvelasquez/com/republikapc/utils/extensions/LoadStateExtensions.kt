package ariesvelasquez.com.republikapc.utils.extensions

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState

fun CombinedLoadStates.finishedPrepend() : Boolean {
    return this.prepend.endOfPaginationReached &&
            this.prepend is LoadState.NotLoading
}

fun CombinedLoadStates.finishedAppend() : Boolean {
    return this.append.endOfPaginationReached &&
            this.append is LoadState.NotLoading
}

fun CombinedLoadStates.isInitialLoading() : Boolean {
    return this.prepend is LoadState.Loading
}

fun CombinedLoadStates.sourceRefreshState() : Boolean {
    return this.source.refresh is LoadState.Loading
}

fun CombinedLoadStates.mediatorRefreshState() : Boolean {
    return this.mediator?.refresh is LoadState.Loading
}

fun CombinedLoadStates.mediatorLastPageReached() : Boolean {
    return this.mediator?.append?.endOfPaginationReached == true  &&
            this.mediator?.prepend?.endOfPaginationReached == true
}