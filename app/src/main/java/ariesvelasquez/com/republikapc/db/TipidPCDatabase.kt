package ariesvelasquez.com.republikapc.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ariesvelasquez.com.republikapc.model.feeds.FeedItem

@Database(
    entities = [FeedItem::class],
    version = 8,
    exportSchema = false
)
abstract class TipidPCDatabase : RoomDatabase() {

    companion object {
        fun create(context: Context): TipidPCDatabase {
            val databaseBuilder = Room.databaseBuilder(context, TipidPCDatabase::class.java, "tipidpc.db")
            return databaseBuilder
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun items(): TipidPCDao

//    companion object {
//
//        @Volatile
//        private var INSTANCE: TipidPCDatabase? = null
//
//        fun getDatabase(context: Context): TipidPCDatabase {
//            return INSTANCE ?: synchronized(this) {
//                Room.databaseBuilder(context, TipidPCDatabase::class.java, "tipid-pc-db")
//                    .fallbackToDestructiveMigration()
//                    .build()
//                    .also { INSTANCE = it }
//            }
//        }
//    }
}