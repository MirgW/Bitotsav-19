package `in`.bitotsav.shared.workers

import `in`.bitotsav.shared.workers.TeamWorkType.*
import `in`.bitotsav.teams.championship.data.ChampionshipTeamRepository
import `in`.bitotsav.teams.nonchampionship.data.NonChampionshipTeamRepository
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.get

private const val TAG = "TeamWorker"

enum class TeamWorkType {
    FETCH_TEAM,
    FETCH_ALL_TEAMS
}

class TeamWorker(context: Context, params: WorkerParameters): Worker(context, params), KoinComponent {

    override fun doWork(): Result {
        val type = valueOf(inputData.getString("type")!!)

        return runBlocking {
            try {
                when (type) {
                    FETCH_ALL_TEAMS -> get<ChampionshipTeamRepository>().fetchAllChampionshipTeamsAsync().await()
                    FETCH_TEAM -> {
                        val eventId = inputData.getInt("eventId", 1)
                        val teamLeaderId = inputData.getString("teamLeaderId")
                            ?: throw Exception("Leader id is empty")
                        get<NonChampionshipTeamRepository>()
                            .fetchNonChampionshipTeamAsync(eventId, teamLeaderId).await()
                    }
                }
                return@runBlocking Result.success()
            } catch (e: Exception) {
                Log.d(TAG, e.message)
                return@runBlocking Result.retry()
            }
        }
    }
}