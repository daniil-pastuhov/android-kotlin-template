package wtf.test.myapplication.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import wtf.test.myapplication.data.models.Product
import wtf.test.myapplication.databinding.ListItemProductBinding

class ProductAdapter : ListAdapter<Product, RecyclerView.ViewHolder>(ProductDiffCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = getItem(position)
        (holder as ProductViewHolder).bind(product)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ProductViewHolder(
            ListItemProductBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    class ProductViewHolder(
        private val binding: ListItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener {
                binding.product?.let { product ->
                    navigateToProduct(product, it)
                }
            }
        }

        private fun navigateToProduct(
            product: Product,
            view: View
        ) {
            val direction =
                MainFragmentDirections.actionMainFragmentToProductDetailFragment(
                    product.productId
                )
            view.findNavController().navigate(direction)
        }

        fun bind(item: Product) {
            binding.apply {
                product = item
                executePendingBindings()
            }
        }
    }
}

private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {

    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}