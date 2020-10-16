package wtf.test.myapplication.ui.main

import android.os.CountDownTimer
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import wtf.test.myapplication.GameRepository
import wtf.test.myapplication.data.models.Choice
import wtf.test.myapplication.data.models.GameRecord
import wtf.test.myapplication.data.models.GameState
import wtf.test.myapplication.data.models.Question

/**
 * The [ViewModel] for fetching a list of [Question]s.
 *
 * The @ExperimentalCoroutinesApi and @FlowPreview indicate that experimental APIs are being used.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class GameViewModel internal constructor(
    private val gameRepository: GameRepository,
    private val gameUuid: String
) : ViewModel() {

    /**
     * Request a snackbar to display a string.
     *
     * This variable is private because we don't want to expose [MutableLiveData].
     *
     * MutableLiveData allows anyone to set a value, and [GameViewModel] is the only
     * class that should be setting values.
     */
    private val _snackbar = MutableLiveData<String?>()

    /**
     * Request a snackbar to display a string.
     */
    val snackbar: LiveData<String?>
        get() = _snackbar

    private val _spinner = MutableLiveData<Boolean>(false)
    /**
     * Show a loading spinner if true
     */
    val spinner: LiveData<Boolean>
        get() = _spinner

    /**
     * Show progress ticks for choices
     */
    val choiceCountDownLiveData = MutableLiveData(0)

    /**
     * The current question selection (flow version)
     */
    private val questionChannel = ConflatedBroadcastChannel<Question>()

    /**
     * A question that updates based on the current game
     */
    val questionLiveData: LiveData<Question> = questionChannel.asFlow().asLiveData()

    /**
     * Game state LiveData
     */
    val gameState: MutableLiveData<GameState> = MutableLiveData(GameState(false, null))

    private var questions: List<Question> = emptyList()
    private var currentQuestionIndex = -1
    private var countDownTimer: CountDownTimer? = null
    private var currentScore = 0

    init {
        gameRepository.getGame(gameUuid)
            .onStart {
                _spinner.value = true
            }
            .mapLatest {
                questions = it.questions
                goToNextQuestion()
            }
            .onEach { _spinner.value = false }
            .onCompletion { _spinner.value = false }
            .catch { throwable -> _snackbar.value = throwable.message }
            .launchIn(viewModelScope)
    }

    private suspend fun goToNextQuestion() {
        if (currentQuestionIndex + 1 >= questions.size) {
            countDownTimer?.cancel()
            finishGame()
            return
        }

        val question = questions[++currentQuestionIndex]
        questionChannel.send(question)

        question.time?.let {
            restartTimer(it.toLong()) {
                choiceCountDownLiveData.postValue(0)
                viewModelScope.launch { goToNextQuestion() }
            }
        }
    }

    private fun restartTimer(durationMillis: Long, onFinishJob: () -> Unit) {
        countDownTimer?.cancel()
        countDownTimer = object: CountDownTimer(durationMillis, 50) {
            override fun onTick(millisUntilFinished: Long) {
                choiceCountDownLiveData.postValue((millisUntilFinished * 1000 / durationMillis).toInt())
            }

            override fun onFinish() {
                onFinishJob.invoke()
            }
        }
        countDownTimer!!.start()
    }

    private fun finishGame() {
        val currentRecord = GameRecord(0, gameUuid, currentScore, questions.size)
        CoroutineScope(Dispatchers.IO).launch {
            gameRepository.addScore(currentRecord)
        }
        viewModelScope.launch {
            val lastScore = gameRepository.getHighScore(gameUuid)
            if (currentRecord.percent > lastScore?.percent ?: 0) {
                gameState.value = GameState(true, currentRecord)
            } else {
                gameState.value = GameState(true, null)
            }
        }
    }

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackbar.value = null
    }

    /**
     * Called when a user does a [choice]
     */
    fun onChoiceDone(choice: Choice) {
        if (choice.correct)
            currentScore++

        restartTimer(2000) {
            viewModelScope.launch {
                goToNextQuestion()
            }
        }
    }
}
