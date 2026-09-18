package com.jaxia.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jaxia.app.data.dao.AvatarDao
import com.jaxia.app.data.dao.CommunityDao
import com.jaxia.app.data.dao.GarmentDao
import com.jaxia.app.data.dao.OutfitDao
import com.jaxia.app.data.dao.SecondChanceDao
import com.jaxia.app.data.model.AvatarProfile
import com.jaxia.app.data.model.CommunityPost
import com.jaxia.app.data.model.Garment
import com.jaxia.app.data.model.Outfit
import com.jaxia.app.data.model.OutfitFeedback
import com.jaxia.app.data.model.SecondChanceItem

@Database(
    entities = [
        Garment::class,
        AvatarProfile::class,
        Outfit::class,
        OutfitFeedback::class,
        CommunityPost::class,
        SecondChanceItem::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun garmentDao(): GarmentDao
    abstract fun avatarDao(): AvatarDao
    abstract fun outfitDao(): OutfitDao
    abstract fun communityDao(): CommunityDao
    abstract fun secondChanceDao(): SecondChanceDao

    companion object {
        private const val DATABASE_NAME = "jaxia_database"

        /**
         * v2 → v3: drops the three `scan*Photo` columns from `avatar_profile`.
         *
         * They were written by the simulated body-scan flow and never held a
         * value, but their presence implied the app stored body photographs —
         * which contradicted the Data Safety declaration (AUDITORIA.md B-05).
         *
         * SQLite before 3.35 has no `DROP COLUMN`, and `minSdk = 24` ships far
         * older versions, so the table is rebuilt the portable way: create,
         * copy, drop, rename.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `avatar_profile_new` (
                        `id` INTEGER NOT NULL,
                        `userName` TEXT NOT NULL,
                        `creationMethod` TEXT NOT NULL,
                        `bodyType` TEXT NOT NULL,
                        `heightCm` INTEGER NOT NULL,
                        `skinToneHex` TEXT NOT NULL,
                        `hairColorHex` TEXT NOT NULL,
                        `hairStyle` TEXT NOT NULL,
                        `topSize` TEXT NOT NULL,
                        `bottomSize` TEXT NOT NULL,
                        `footwearSize` TEXT NOT NULL,
                        `stylePreference` TEXT NOT NULL,
                        `comfortPreference` TEXT NOT NULL,
                        `preferredFootwear` TEXT NOT NULL,
                        `isPrivateAccount` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `avatar_profile_new` (
                        `id`, `userName`, `creationMethod`, `bodyType`, `heightCm`,
                        `skinToneHex`, `hairColorHex`, `hairStyle`, `topSize`,
                        `bottomSize`, `footwearSize`, `stylePreference`,
                        `comfortPreference`, `preferredFootwear`, `isPrivateAccount`
                    )
                    SELECT
                        `id`, `userName`, 'manual', `bodyType`, `heightCm`,
                        `skinToneHex`, `hairColorHex`, `hairStyle`, `topSize`,
                        `bottomSize`, `footwearSize`, `stylePreference`,
                        `comfortPreference`, `preferredFootwear`, `isPrivateAccount`
                    FROM `avatar_profile`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `avatar_profile`")
                db.execSQL("ALTER TABLE `avatar_profile_new` RENAME TO `avatar_profile`")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Re-check inside the lock: another thread may have won the race
                // between the null check above and acquiring the monitor.
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // Deliberately NOT fallbackToDestructiveMigration(): that
                    // wiped every garment, outfit and measurement the user had
                    // entered on any schema change (AUDITORIA.md B-06).
                    .addMigrations(MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
