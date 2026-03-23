package io.github.hitchclimber.universaltimers.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BundleEntity::class], version = 1)
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
                ).build().also { instance = it }
            }
    }
}
