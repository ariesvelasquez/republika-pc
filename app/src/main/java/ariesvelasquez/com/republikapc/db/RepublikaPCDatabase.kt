package ariesvelasquez.com.republikapc.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigparts.RigPart
import ariesvelasquez.com.republikapc.model.saved.Saved

@Database(
    entities = [FeedItem::class, Saved::class, RigPart::class],
    version = 2,
    exportSchema = false
)
abstract class RepublikaPCDatabase : RoomDatabase() {

    companion object {
        fun create(context: Context): RepublikaPCDatabase {
            val databaseBuilder = Room.databaseBuilder(context, RepublikaPCDatabase::class.java, "tipidpc.db")
            return databaseBuilder
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun items(): RepublikaPCDao

//    companion object {
//
//        @Volatile
//        private var INSTANCE: RepublikaPCDatabase? = null
//
//        fun getDatabase(context: Context): RepublikaPCDatabase {
//            return INSTANCE ?: synchronized(this) {
//                Room.databaseBuilder(context, RepublikaPCDatabase::class.java, "tipid-pc-db")
//                    .fallbackToDestructiveMigration()
//                    .build()
//                    .also { INSTANCE = it }
//            }
//        }
//    }
}