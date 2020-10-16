package wtf.test.myapplication.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import wtf.test.myapplication.GameRepository
import wtf.test.myapplication.R
import wtf.test.myapplication.data.models.Choice
import wtf.test.myapplication.databinding.QuestionsFragmentBinding
import wtf.test.myapplication.utils.Injector

@FlowPreview
@ExperimentalCoroutinesApi
class QuestionsFragment : Fragment(R.layout.questions_fragment) {

    private val viewModel: GameViewModel by viewModels {
        Injector.provideGameViewModelFactory(requireContext(), "fb4054fc-6a71-463e-88cd-243876715bc1")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        val binding = if (root == null) QuestionsFragmentBinding.inflate(inflater, container, false) else QuestionsFragmentBinding.bind(root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        context ?: return binding.root

        // show the spinner when [ViewModel.spinner] is true
        viewModel.spinner.observe(viewLifecycleOwner) { show ->
            binding.spinner.visibility = if (show) View.VISIBLE else View.GONE
        }

        // Show a snackbar whenever the [ViewModel.snackbar] is updated a non-null value
        viewModel.snackbar.observe(viewLifecycleOwner) { text ->
            text?.let {
                Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
                viewModel.onSnackbarShown()
            }
        }

        val adapter = ChoiceAdapter(object : OnChoiceClickedListener {
            override fun onChoiceClicked(choice: Choice) {
                viewModel.onChoiceDone(choice)
            }
        })
        binding.answerList.adapter = adapter

        viewModel.questionLiveData.observe(viewLifecycleOwner) { question ->
            adapter.submitList(question.choices)
        }

        viewModel.gameState.observe(viewLifecycleOwner) { gameState ->
            if (gameState.finished) {
                if (gameState.newHighScore != null) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.info_game_finished)
                        .setMessage(getString(R.string.info_game_score, gameState.newHighScore.score, gameState.newHighScore.maxScore))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok) { _, _ -> activity?.finish() }
                        .show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.info_game_finished)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok) { _, _ -> activity?.finish() }
                        .show()
                }
            }
        }

        return binding.root
    }
}

/**
 * Factory for creating a [GameViewModel] with a constructor that takes a [GameRepository] and game uuid.
 */
@FlowPreview
@ExperimentalCoroutinesApi
class GameViewModelFactory(
    private val repository: GameRepository,
    private val gameUuid: String
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = GameViewModel(repository, gameUuid) as T
}

