package ariesvelasquez.com.republikapc.utils

import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource

class TransformUtil {

    fun resourceToModel(reponseList: List<FeedItemsResource>) : List<FeedItem>{
        return mutableListOf<FeedItem>().apply {
            reponseList.forEachIndexed { _, response ->
//                add(FeedItem(title = response.title))
            }
        }
    }
}