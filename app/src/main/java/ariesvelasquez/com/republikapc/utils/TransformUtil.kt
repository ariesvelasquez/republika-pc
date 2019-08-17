package ariesvelasquez.com.republikapc.utils

import ariesvelasquez.com.republikapc.model.SellingItem
import ariesvelasquez.com.republikapc.model.SellingItemResource

class TransformUtil {

    fun resourceToModel(reponseList: List<SellingItemResource>) : List<SellingItem>{
        return mutableListOf<SellingItem>().apply {
            reponseList.forEachIndexed { _, response ->
                add(SellingItem(title = response.title))
            }
        }
    }
}