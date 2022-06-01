package ariesvelasquez.com.republikapc.datasource.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ariesvelasquez.com.republikapc.model.cached_key.SellerCachedKey
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigparts.RigPart

@Database(
    entities = [FeedItem::class, RigPart::class, SellerCachedKey::class],
    version = 2,
    exportSchema = false
)
abstract class RepublikaPCDatabase : RoomDatabase() {

    companion object {
        fun create(context: Context): RepublikaPCDatabase {
            val databaseBuilder = Room.databaseBuilder(context, RepublikaPCDatabase::class.java, "republikapc.db")
            return databaseBuilder
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun items(): RepublikaPCDao
    abstract fun sellerCachedKeyDao() : SellerCachedKeyDao
}