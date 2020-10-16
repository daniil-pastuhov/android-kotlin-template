package wtf.test.myapplication.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import wtf.test.myapplication.GameRepository
import wtf.test.myapplication.data.network.NetworkService
import wtf.test.myapplication.ui.main.GameViewModelFactory

interface ViewModelFactoryProvider {
    fun provideGameViewModelFactory(context: Context, gameUuid: String): GameViewModelFactory
}

val Injector: ViewModelFactoryProvider
    get() = currentInjector

private object DefaultViewModelProvider: ViewModelFactoryProvider {
    private fun getProductRepository(context: Context): GameRepository {
        return GameRepository.getInstance(
            gameRecordDao(context),
            dataService()
        )
    }

    private fun dataService() = NetworkService()

    private fun gameRecordDao(context: Context) =
        AppDatabase.getInstance(context.applicationContext).gameRecordDao()

    override fun provideGameViewModelFactory(context: Context, gameUuid: String): GameViewModelFactory = GameViewModelFactory(getProductRepository(context), gameUuid)
}

private object Lock

@Volatile private var currentInjector: ViewModelFactoryProvider =
    DefaultViewModelProvider


@VisibleForTesting
private fun setInjectorForTesting(injector: ViewModelFactoryProvider?) {
    synchronized(Lock) {
        currentInjector = injector ?: DefaultViewModelProvider
    }
}

@VisibleForTesting
private fun resetInjector() =
    setInjectorForTesting(null)