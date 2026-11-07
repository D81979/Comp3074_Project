package ca.gbc.comp3074.scavengerhuntgroup95project.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Register the three database tables.
@Database(
    entities = [
        PointOfInterest::class,
        Tag::class,
        PointTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
abstract class HuntDatabase : RoomDatabase() {

    // Give the app access to database operations.
    abstract fun huntDao(): HuntDao

    companion object {

        // Keep one shared database instance.
        @Volatile
        private var INSTANCE: HuntDatabase? = null

        fun getDatabase(context: Context): HuntDatabase {
            return INSTANCE ?: synchronized(this) {
                val database = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HuntDatabase::class.java,
                    "scavenger_hunt.db"
                ).build()

                INSTANCE = database
                database
            }
        }
    }
}