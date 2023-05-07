package mollah.yamin.autoslidingcarousel.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import mollah.yamin.autoslidingcarousel.R
import mollah.yamin.autoslidingcarousel.databinding.CarouselIndicatorItemBinding

class CarouselIndicatorAdapter internal constructor(
    private val callback: ((Int) -> Unit)
) : RecyclerView.Adapter<CarouselIndicatorAdapter.ViewHolder>() {

    private var noOfSlides: Int = 0
    private var checkedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: CarouselIndicatorItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_carousel_indicator, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return noOfSlides
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setNumberOfSlides(number: Int) {
        noOfSlides = number
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setIndicatorAt(position: Int) {
        if (position in 0 until noOfSlides) {
            checkedPosition = position
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder (private val binding: CarouselIndicatorItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind() {
            if (checkedPosition == adapterPosition) {
                binding.indicator.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.theme_red))
            } else {
                binding.indicator.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.theme_dark_blue))
            }
            binding.indicator.setOnClickListener {
                binding.indicator.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.theme_red))
                if (checkedPosition != adapterPosition) {
                    notifyItemChanged(checkedPosition)
                    checkedPosition = adapterPosition
                }
                callback.invoke(adapterPosition)
                notifyDataSetChanged()
            }
        }
    }
}
