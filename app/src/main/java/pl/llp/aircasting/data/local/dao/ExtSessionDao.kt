package pl.llp.aircasting.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport

@Dao
interface ExtSessionDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<CrashlyticsReport.Session.User>

    @Insert
    fun insertAll(vararg users: CrashlyticsReport.Session.User)

    @Delete
    fun delete(user: CrashlyticsReport.Session.User)
}