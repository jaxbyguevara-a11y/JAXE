package com.jaxia.app

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.jaxia.app.data.database.AppDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises `MIGRATION_2_3` against a hand-built v2 database.
 *
 * The usual tool for this is Room's `MigrationTestHelper`, but it needs the v2
 * schema JSON — and the original project shipped `exportSchema = false`, so that
 * file never existed (AUDITORIA.md M-05). The v2 table is therefore recreated
 * here with raw SQL, which is what the migration will actually meet on a device
 * that already has the app installed.
 *
 * What is being defended: the original builder called
 * `fallbackToDestructiveMigration()`, so any schema change wiped the user's
 * entire closet and avatar (B-06). These assertions fail if that behaviour ever
 * comes back.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AvatarMigrationTest {

    private lateinit var helper: SupportSQLiteOpenHelper

    /** Schema of `avatar_profile` as it existed at version 2, scan columns included. */
    private val createV2 = """
        CREATE TABLE IF NOT EXISTS `avatar_profile` (
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
            `hasScanConsent` INTEGER NOT NULL,
            `isPrivateAccount` INTEGER NOT NULL,
            `scanFrontPhoto` TEXT NOT NULL,
            `scanProfilePhoto` TEXT NOT NULL,
            `scanBackPhoto` TEXT NOT NULL,
            PRIMARY KEY(`id`)
        )
    """.trimIndent()

    private fun openV2(): SupportSQLiteDatabase {
        val context: Context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase("migration-test.db")
        helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name("migration-test.db")
                .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: SupportSQLiteDatabase) = db.execSQL(createV2)
                    override fun onUpgrade(db: SupportSQLiteDatabase, old: Int, new: Int) = Unit
                })
                .build()
        )
        return helper.writableDatabase
    }

    private fun columnsOf(db: SupportSQLiteDatabase, table: String): Set<String> =
        db.query("PRAGMA table_info(`$table`)").use { c ->
            buildSet {
                val nameIdx = c.getColumnIndex("name")
                while (c.moveToNext()) add(c.getString(nameIdx))
            }
        }

    @After
    fun tearDown() {
        if (::helper.isInitialized) helper.close()
    }

    @Test
    fun `migration preserves the user's avatar data`() {
        val db = openV2()
        db.execSQL(
            """
            INSERT INTO `avatar_profile` VALUES (
                1, 'Camila', 'scan', 'Reloj de arena', 166,
                '#D99B77', '#321E17', 'Ondulado largo',
                'M', '38 / M', '37',
                'Elegante y consciente', 'Equilibrado', 'Tenis y mocasines',
                1, 1, '', '', ''
            )
            """.trimIndent()
        )

        AppDatabase.MIGRATION_2_3.migrate(db)

        db.query("SELECT * FROM `avatar_profile` WHERE id = 1").use { c ->
            assertTrue("la fila del usuario debe sobrevivir", c.moveToFirst())
            assertEquals("Camila", c.getString(c.getColumnIndexOrThrow("userName")))
            assertEquals(166, c.getInt(c.getColumnIndexOrThrow("heightCm")))
            assertEquals("M", c.getString(c.getColumnIndexOrThrow("topSize")))
            assertEquals("#D99B77", c.getString(c.getColumnIndexOrThrow("skinToneHex")))
            assertEquals(
                "Reloj de arena",
                c.getString(c.getColumnIndexOrThrow("bodyType")),
            )
        }
    }

    @Test
    fun `migration drops the scan columns`() {
        val db = openV2()
        db.execSQL(
            """
            INSERT INTO `avatar_profile` VALUES (
                1, 'Camila', 'scan', 'Rectangular', 170,
                '#D59E7C', '#3C2419', 'Lacio medio',
                'S', '36', '36',
                'Casual', 'Equilibrado', 'Tenis',
                1, 1, 'ruta/frente.jpg', 'ruta/perfil.jpg', 'ruta/espalda.jpg'
            )
            """.trimIndent()
        )
        assertTrue("precondición: v2 tiene las columnas de escaneo",
            columnsOf(db, "avatar_profile").containsAll(
                setOf("scanFrontPhoto", "scanProfilePhoto", "scanBackPhoto", "hasScanConsent")
            )
        )

        AppDatabase.MIGRATION_2_3.migrate(db)

        val cols = columnsOf(db, "avatar_profile")
        for (gone in listOf("scanFrontPhoto", "scanProfilePhoto", "scanBackPhoto", "hasScanConsent")) {
            assertFalse("la columna $gone debe desaparecer", gone in cols)
        }
        assertTrue("las columnas útiles se conservan",
            cols.containsAll(setOf("id", "userName", "heightCm", "bodyType", "topSize")))
    }

    @Test
    fun `migration normalises creationMethod to manual`() {
        val db = openV2()
        db.execSQL(
            """
            INSERT INTO `avatar_profile` VALUES (
                1, 'Camila', 'scan', 'Ovalado', 160,
                '#D59E7C', '#3C2419', 'Corte Bob',
                'L', '42', '38',
                'Cómodo', 'Muy holgado', 'Flats',
                1, 0, '', '', ''
            )
            """.trimIndent()
        )

        AppDatabase.MIGRATION_2_3.migrate(db)

        db.query("SELECT creationMethod, isPrivateAccount FROM `avatar_profile` WHERE id = 1").use { c ->
            assertTrue(c.moveToFirst())
            // The simulated scan is gone, so no row may still claim to be one.
            assertEquals("manual", c.getString(0))
            assertEquals("isPrivateAccount se conserva", 0, c.getInt(1))
        }
    }

    @Test
    fun `migration keeps every row, not just the first`() {
        val db = openV2()
        for (id in 1..3) {
            db.execSQL(
                """
                INSERT INTO `avatar_profile` VALUES (
                    $id, 'Perfil $id', 'manual', 'Rectangular', ${160 + id},
                    '#D59E7C', '#3C2419', 'Lacio medio',
                    'M', '38', '37', 'Casual', 'Equilibrado', 'Tenis',
                    1, 1, '', '', ''
                )
                """.trimIndent()
            )
        }

        AppDatabase.MIGRATION_2_3.migrate(db)

        db.query("SELECT COUNT(*) FROM `avatar_profile`").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(3, c.getInt(0))
        }
    }
}
