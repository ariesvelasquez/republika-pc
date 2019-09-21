package ariesvelasquez.com.republikapc.repository.dashboard

import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.RigItem
import ariesvelasquez.com.republikapc.repository.Listing

interface IDashboardRepository {
    fun feeds(): Listing<FeedItem>

    fun rigs(): Listing<RigItem>
}