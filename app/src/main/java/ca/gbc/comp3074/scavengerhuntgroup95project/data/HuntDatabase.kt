package ca.gbc.comp3074.scavengerhuntgroup95project.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Register the app's database tables.
@Database(
    entities = [
        PointOfInterest::class,
        Tag::class,
        PointTagCrossRef::class,
        TeamMember::class
    ],
    version = 2,
    exportSchema = true
)
abstract class HuntDatabase : RoomDatabase() {

    // Give the app access to database operations.
    abstract fun huntDao(): HuntDao

    companion object {

        // Keep one shared database instance.
        @Volatile
        private var INSTANCE: HuntDatabase? = null

        // Add members without deleting saved locations.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `team_members` (
                        `memberId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `phone` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): HuntDatabase {
            return INSTANCE ?: synchronized(this) {
                val database = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HuntDatabase::class.java,
                    "scavenger_hunt.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = database
                database
            }
        }
    }
}