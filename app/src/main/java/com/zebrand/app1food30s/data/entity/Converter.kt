package com.zebrand.app1food30s.data.entity

import androidx.room.TypeConverter
import java.util.Date

class Converter {
    @TypeConverter
    fun dateToLong(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun longToDate(time: Long?): Date? {
        return time?.let { Date(it) }
    }
}