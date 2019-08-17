package ariesvelasquez.com.republikapc.network

data class Resource<ResultType>(var status: Status, var data: ResultType? = null, var errorMessage: String? = null) {

    companion object {

        /**
         * Creates [Resource] object with 'SUCCESS' status and [data].
         */
        fun <ResultType> success(data: ResultType) : Resource<ResultType> =
            Resource(
                Status.SUCCESS,
                data
            )

        /**
         * Creates [Resource] object with 'LOADING' status to notify observers (UI) to show loading.
         */
        fun <ResultType> loading(): Resource<ResultType> =
            Resource(Status.LOADING)

        /**
         * Creates [Resource] object with 'ERROR' status and [message]
         */
        fun <ResultType> error(message: String): Resource<ResultType> =
            Resource(
                Status.ERROR,
                errorMessage = message
            )
    }
}