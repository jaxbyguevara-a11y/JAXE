package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AvatarDao
import com.example.data.dao.CommunityDao
import com.example.data.dao.GarmentDao
import com.example.data.dao.OutfitDao
import com.example.data.dao.SecondChanceDao
import com.example.data.model.AvatarProfile
import com.example.data.model.CommunityPost
import com.example.data.model.Garment
import com.example.data.model.Outfit
import com.example.data.model.OutfitFeedback
import com.example.data.model.SecondChanceItem

@Database(
    entities = [
        Garment::class,
        AvatarProfile::class,
        Outfit::class,
        OutfitFeedback::class,
        CommunityPost::class,
        SecondChanceItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun garmentDao(): GarmentDao
    abstract fun avatarDao(): AvatarDao
    abstract fun outfitDao(): OutfitDao
    abstract fun communityDao(): CommunityDao
    abstract fun secondChanceDao(): SecondChanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lookia_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
