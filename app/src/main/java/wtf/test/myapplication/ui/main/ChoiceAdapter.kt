package wtf.test.myapplication.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import wtf.test.myapplication.R
import wtf.test.myapplication.data.models.Choice
import wtf.test.myapplication.databinding.ListItemChoiceBinding

class ChoiceAdapter(private val onChoiceClickedListener: OnChoiceClickedListener)
    : ListAdapter<Choice, RecyclerView.ViewHolder>(ChoiceDiffCallback()) {

    var selectedChoice: Choice? = null
    private val isAnswerRecorded: Boolean
        get() = selectedChoice != null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val choice = getItem(position)
        (holder as ChoiceViewHolder).bind(choice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ChoiceViewHolder(
            ListItemChoiceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false), onChoiceClickedListener)
    }

    inner class ChoiceViewHolder(
        private val binding: ListItemChoiceBinding,
        private val onChoiceClickedListenerListener: OnChoiceClickedListener
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener {
                binding.choice?.let { choice ->
                    choiceSelected(choice, it)
                }
            }
        }

        private fun choiceSelected(
            choice: Choice,
            view: View
        ) {
            if (isAnswerRecorded) return

            selectedChoice = choice

            //redraw
            notifyDataSetChanged()

            onChoiceClickedListenerListener.onChoiceClicked(choice)
        }

        fun bind(item: Choice) {
            binding.apply {
                choice = item
                choiceView.setCardBackgroundColor(ResourcesCompat.getColor(itemView.resources, getBackgroundColor(item), null))
                executePendingBindings()
            }
        }
    }

    @ColorRes
    private fun getBackgroundColor(choice: Choice): Int {
        return if (!isAnswerRecorded) R.color.gray_300
        else if (choice.correct) R.color.correct_choice
        else if (choice == selectedChoice) R.color.wrong_choice
        else R.color.gray_300
    }

    override fun submitList(list: List<Choice>?) {
        selectedChoice = null
        super.submitList(list)
    }
}

interface OnChoiceClickedListener {
    fun onChoiceClicked(choice: Choice)
}

private class ChoiceDiffCallback : DiffUtil.ItemCallback<Choice>() {

    override fun areItemsTheSame(oldItem: Choice, newItem: Choice): Boolean {
        return oldItem.answer == newItem.answer && oldItem.correct == newItem.correct
    }

    override fun areContentsTheSame(oldItem: Choice, newItem: Choice): Boolean {
        return oldItem == newItem
    }
}