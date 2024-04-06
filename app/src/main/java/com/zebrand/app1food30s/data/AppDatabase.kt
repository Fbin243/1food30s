    package com.zebrand.app1food30s.data

    import android.content.Context
    import androidx.room.Database
    import androidx.room.Room
    import androidx.room.RoomDatabase
    import androidx.room.TypeConverters
    import com.zebrand.app1food30s.data.dao.CartDao
    import com.zebrand.app1food30s.data.dao.CartItemDao
    import com.zebrand.app1food30s.data.dao.CategoryDao
    import com.zebrand.app1food30s.data.dao.OfferDao
    import com.zebrand.app1food30s.data.dao.ProductDao
    import com.zebrand.app1food30s.data.entity.CartEntity
    import com.zebrand.app1food30s.data.entity.CartItemEntity
    import com.zebrand.app1food30s.data.entity.Category
    import com.zebrand.app1food30s.data.entity.Converter
    import com.zebrand.app1food30s.data.entity.Offer
    import com.zebrand.app1food30s.data.entity.Product

    @Database(entities = [
        Category::class,
        Product::class,
        Offer::class,
        CartEntity::class,
        CartItemEntity::class
                         ], version = 1)
    @TypeConverters(Converter::class)
    abstract class AppDatabase: RoomDatabase() {
        abstract fun categoryDao(): CategoryDao
        abstract fun productDao(): ProductDao
        abstract fun offerDao(): OfferDao
        abstract fun cartDao(): CartDao
        abstract fun cartItemDao(): CartItemDao
        companion object {
            private const val DB_NAME = "DB_App1Food30s"
            private var instance: AppDatabase? = null

            fun getInstance(context: Context): AppDatabase {
                return instance ?: buildDatabase(context).also { instance = it }
            }

            private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                    .allowMainThreadQueries().build()
        }
    }