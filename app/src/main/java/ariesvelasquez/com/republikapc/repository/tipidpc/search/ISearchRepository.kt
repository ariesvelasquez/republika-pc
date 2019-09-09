package ariesvelasquez.com.republikapc.repository.tipidpc.search

import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.Listing

interface ISearchRepository {
    // Items for TipidPc Users result

    // Items for TipidPc Items result
    fun searchItems(searchVal: String) : Listing<FeedItem>
}