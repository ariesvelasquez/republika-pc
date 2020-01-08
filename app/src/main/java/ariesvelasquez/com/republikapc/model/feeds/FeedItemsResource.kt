package ariesvelasquez.com.republikapc.model.feeds

data class FeedItemsResource (
    var isListEmpty: Boolean = false,
    var page: String = "",
    var items: List<FeedItem>? = null
)