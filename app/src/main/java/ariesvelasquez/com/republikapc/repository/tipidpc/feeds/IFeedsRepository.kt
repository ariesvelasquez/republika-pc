package ariesvelasquez.com.republikapc.repository.tipidpc.feeds

import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.Listing

interface IFeedsRepository {
    fun feeds(): Listing<FeedItem>
}