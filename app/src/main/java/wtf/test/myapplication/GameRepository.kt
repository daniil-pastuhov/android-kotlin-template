package wtf.test.myapplication

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import wtf.test.myapplication.data.GameRecordDao
import wtf.test.myapplication.data.models.GameModel
import wtf.test.myapplication.data.models.GameRecord
import wtf.test.myapplication.data.models.Question
import wtf.test.myapplication.data.network.NetworkService

/**
 * Repository module for handling data operations.
 *
 * The @ExperimentalCoroutinesApi and @FlowPreview indicate that experimental APIs are being used.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class GameRepository private constructor(
    private val gameRecordDao: GameRecordDao,
    private val dataService: NetworkService,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    suspend fun getHighScore(uuid: String) = gameRecordDao.getGameHighScore(uuid)
    suspend fun addScore(gameRecord: GameRecord) = gameRecordDao.insert(gameRecord)

    /**
     * Fetch a list of [Question]s from the database that matches a given [uuid]. Returns a [Flow].
     */
    fun getQuestions(uuid: String): Flow<List<Question>> {
        // A Flow from Room will return each value, just like a LiveData.
        return flow {
            emit(emptyList<Question>())
//            tryUpdateRecentQuestionsCache(uuid)
            fetchQuestionsForGame(uuid)
        }
    }

    /**
     * Fetch a [GameModel]s from the database that matches a given [uuid]. Returns a [Flow].
     */
    fun getGame(uuid: String): Flow<GameModel> {
        return flow {
            emit(fetchGame(uuid))
        }
    }

    /**
     * Returns true if we should make a network request.
     */
    private suspend fun shouldUpdateQuestionsCache(): Boolean {
        // suspending function, so you can e.g. check the status of the database here
        return true
    }

    /**
     * Update the questions cache.
     *
     * This function may decide to avoid making a network requests on every call based on a
     * cache-invalidation policy.
     */
    suspend fun tryUpdateRecentQuestionsCache(uuid: String) {
        if (shouldUpdateQuestionsCache()) fetchRecentQuestions(uuid)
    }

    /**
     * Fetch a new list of questions from the network, and append them to db
     */
    private suspend fun fetchRecentQuestions(uuid: String) {
        val questions = fetchGame(uuid).questions
        // TODO: add me to db
//        questionsDao.insert(game.questions)
    }

    /**
     * Fetch a game from the network
     */
    private suspend fun fetchGame(uuid: String): GameModel = withContext(defaultDispatcher) {
        val game = dataService.getGameApiCall(uuid)
        // TODO: add me to db
//        gameDao.insert(game)
        game
    }

    /**
     * Fetch a list of questions from the network, and append them to cache
     */
    private suspend fun fetchQuestionsForGame(uuid: String): List<Question> = withContext(defaultDispatcher) {
        val game = fetchGame(uuid)
        // TODO: add me to db
//        questionsDao.insert(game.questions)
        game.questions
    }

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: GameRepository? = null

        fun getInstance(gameRecordDao: GameRecordDao, dataService: NetworkService) =
            instance ?: synchronized(this) {
                instance ?: GameRepository(gameRecordDao, dataService).also { instance = it }
            }
    }
}
