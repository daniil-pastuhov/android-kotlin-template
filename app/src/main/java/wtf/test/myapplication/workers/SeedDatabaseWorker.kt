package wtf.test.myapplication.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.coroutineScope
import wtf.test.myapplication.data.models.GameModel
import wtf.test.myapplication.utils.AppDatabase

class SeedDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            applicationContext.assets.open("test_quiz.json").use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val gameType = object : TypeToken<GameModel>() {}.type
                    val gameModel: GameModel = Gson().fromJson(jsonReader, gameType)

                    val database = AppDatabase.getInstance(applicationContext)
                    // TODO: use db
//                    database.questionsDao().insertAll(gameModel.questions)

                    Result.success()
                }
            }
        } catch (ex: Exception) {
            Log.e("SeedDatabaseWorker", "Error seeding database", ex)
            Result.failure()
        }
    }
}