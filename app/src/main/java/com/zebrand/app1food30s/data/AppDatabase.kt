    package com.zebrand.app1food30s.data

    import android.content.Context
    import androidx.room.Database
    import androidx.room.Room
    import androidx.room.RoomDatabase
    import androidx.room.TypeConverters
    import com.zebrand.app1food30s.data.dao.CategoryDao
    import com.zebrand.app1food30s.data.entity.Category
    import com.zebrand.app1food30s.data.entity.Converter

    @Database(entities = [Category::class], version = 1)
    @TypeConverters(Converter::class)
    abstract class AppDatabase: RoomDatabase() {
        abstract fun categoryDao(): CategoryDao
    //    abstract fun productDAO(): ProductDAO
        companion object {
            private const val DB_NAME = "DB_App1Food30s"
            private var instance: AppDatabase? = null

            fun getInstance(context: Context): AppDatabase {
                return instance ?: buildDatabase(context).also { instance = it }
            }

            private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                    .allowMainThreadQueries().build()

            fun deleteDatabase(context: Context) {
                context.deleteDatabase(DB_NAME)
                instance = null
            }
        }
    }