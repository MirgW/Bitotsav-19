package `in`.bitotsav.shared

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose

object Singleton {
    const val baseUrl = "https://bitotsav.in/api/app/"

//    koine!
//    val database = SingletonHolder<AppDatabase, Context> {
//        Room.databaseBuilder(it.applicationContext,
//            AppDatabase::class.java, "App.db")
//            .build()
//    }

    val gson by lazy {
        GsonBuilder().addDeserializationExclusionStrategy(
            object: ExclusionStrategy {
                override fun shouldSkipField(fieldAttributes: FieldAttributes):Boolean {
                    val expose = fieldAttributes.getAnnotation(Expose::class.java)
                    return expose != null && !expose.deserialize
                }
                override fun shouldSkipClass(aClass:Class<*>):Boolean {
                    return false
                }
            }
        ).create()
    }
}