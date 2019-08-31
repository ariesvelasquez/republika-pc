package ariesvelasquez.com.republikapc.model.feeds

data class FeedItemsResource (
    var page: String = "",
    var items: List<FeedItem>? = null
)