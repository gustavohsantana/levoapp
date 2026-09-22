package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.Delivery
import com.example.data.model.SyncAction

class Converters {
    @TypeConverter
    fun fromOrderStage(stage: com.example.data.model.OrderStage): String = stage.name

    @TypeConverter
    fun toOrderStage(value: String): com.example.data.model.OrderStage =
        com.example.data.model.OrderStage.fromString(value)

    @TypeConverter
    fun fromOrderSourceKind(source: com.example.data.model.OrderSourceKind): String = source.name

    @TypeConverter
    fun toOrderSourceKind(value: String): com.example.data.model.OrderSourceKind =
        com.example.data.model.OrderSourceKind.fromString(value)

    @TypeConverter
    fun fromOrderFulfillment(fulfillment: com.example.data.model.OrderFulfillment): String = fulfillment.name

    @TypeConverter
    fun toOrderFulfillment(value: String): com.example.data.model.OrderFulfillment =
        try { com.example.data.model.OrderFulfillment.valueOf(value) } catch (_: Exception) { com.example.data.model.OrderFulfillment.DELIVERY }

    @TypeConverter
    fun fromPaymentMethodKind(method: com.example.data.model.PaymentMethodKind): String = method.name

    @TypeConverter
    fun toPaymentMethodKind(value: String): com.example.data.model.PaymentMethodKind =
        com.example.data.model.PaymentMethodKind.fromString(value)
}

@Database(
    entities = [Delivery::class, SyncAction::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LevoDatabase : RoomDatabase() {
    abstract fun deliveryDao(): DeliveryDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: LevoDatabase? = null

        fun getDatabase(context: Context): LevoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LevoDatabase::class.java,
                    "levo_delivery_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
