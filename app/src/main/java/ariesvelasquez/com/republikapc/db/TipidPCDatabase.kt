package ariesvelasquez.com.republikapc.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ariesvelasquez.com.republikapc.model.SellingItem

@Database (entities = [SellingItem::class], version = 1)
abstract class TipidPCDatabase: RoomDatabase() {

    abstract fun tipidPCDao(): TipidPCDao

    companion object {

        @Volatile
        private var INSTANCE: TipidPCDatabase? = null

        fun getDatabase(context: Context) : TipidPCDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, TipidPCDatabase::class.java, "tipid-pc-db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}