package io.github.hitchclimber.universaltimers.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// TODO: Replace fallbackToDestructiveMigration with a proper Migration(1, 2)
//  that ALTERs the bundles table to add the countdownEnabled column.
@Database(entities = [BundleEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bundleDao(): BundleDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timerbundle.db",
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
