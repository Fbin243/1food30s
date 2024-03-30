package com.zebrand.app1food30s.data.entity

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.DocumentReference
import com.google.gson.Gson
import com.zebrand.app1food30s.utils.FirebaseUtils
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

    @TypeConverter
    fun fromDocumentReference(documentReference: DocumentReference?): String? {
        return documentReference?.path
    }

    @TypeConverter
    fun toDocumentReference(path: String?): DocumentReference? {
        return path?.let { FirebaseUtils.fireStore.document(it) }
    }

    @TypeConverter
    fun fromReviewList(reviews: List<Review>?): String? {
        if (reviews == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Review>>() {}.type
        return gson.toJson(reviews, type)
    }

    @TypeConverter
    fun toReviewList(reviewsString: String?): List<Review>? {
        if (reviewsString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Review>>() {}.type
        return gson.fromJson(reviewsString, type)
    }
}
