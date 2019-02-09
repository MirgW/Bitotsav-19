package `in`.bitotsav

import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.koin.repositoriesModule
import `in`.bitotsav.koin.retrofitModule
import `in`.bitotsav.koin.sharedPrefsModule
import `in`.bitotsav.koin.viewModelsModule
import `in`.bitotsav.notification.utils.createNotificationChannels
import `in`.bitotsav.shared.utils.getWork
import `in`.bitotsav.shared.utils.scheduleWork
import `in`.bitotsav.shared.utils.startReminderWork
import `in`.bitotsav.shared.workers.*
import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.util.*

class Bitotsav19 : Application() {
    companion object {
        private const val IS_FIRST_RUN = "isFirstRun"
        private const val TAG = "Bitotsav19"
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseMessaging.getInstance().subscribeToTopic("global")
            .addOnCompleteListener { task ->
                var msg = "Subscription to global successful"
                if (!task.isSuccessful) {
                    msg = "Subscription to global not successful"
                }
                Log.d(TAG, msg)
            }

        startKoin {
            androidContext(this@Bitotsav19)
            // Enable logging, log.INFO by default
            androidLogger()
            // Use properties from assets/koin.properties
            androidFileProperties()
            modules(repositoriesModule, retrofitModule, viewModelsModule, sharedPrefsModule)
        }

        checkAndScheduleReminderWork()

        if (get<SharedPreferences>().getBoolean(IS_FIRST_RUN, true)) {
            init()
            get<SharedPreferences>().edit().putBoolean(IS_FIRST_RUN, false).apply()
        }

        val eventWork =
            getWork<EventWorker>(workDataOf("type" to EventWorkType.FETCH_ALL_EVENTS.name))
        val winningTeamsWork =
            getWork<ResultWorker>(workDataOf("type" to ResultWorkType.WINNING_TEAMS.name))
        WorkManager.getInstance().beginWith(eventWork).then(winningTeamsWork).enqueue()
        scheduleWork<FeedWorker>(
            workDataOf("type" to FeedWorkType.FETCH_FEEDS.name)
        )
    }

    // Place code which needs to run on first run only here
    private fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(this)
        }

        get<EventRepository>().getEventsFromLocalJson()
    }

    private fun checkAndScheduleReminderWork() {
        val currentTimestamp = System.currentTimeMillis()
        val calendar = GregorianCalendar(TimeZone.getTimeZone("Asia/Kolkata"))
        calendar.set(2019, 1, 17, 20, 0)
        val endOfBitotsav = calendar.timeInMillis
        if (currentTimestamp < endOfBitotsav)
            startReminderWork()
    }
}

// Global TODOs
// TODO: Move tools:context in all layout files from <layout> to root inside <layout>
// TODO: [Refactor] The magnum opus: Wire color change into the lifecycle, so every
//  major interactions causes a color change!
// TODO: [WARN]: Figure out if lifecycleOwner passed to data binding should be
//  viewLifecycleOwner or
// TODO: [Refactor] Inject all services as AuthenticationService is right now
