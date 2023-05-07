package mollah.yamin.autoslidingcarousel.ui.adapters

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import mollah.yamin.autoslidingcarousel.R
import mollah.yamin.autoslidingcarousel.databinding.CarouselItemBinding
import mollah.yamin.autoslidingcarousel.models.Carousel
import mollah.yamin.autoslidingcarousel.utils.AppConstants.MINIMUM_SLIDER_NUMBER
import mollah.yamin.autoslidingcarousel.utils.AppConstants.SLIDER_NUMBER_MULTIPLIER
import com.bumptech.glide.request.target.Target

class CarouselAdapter internal constructor(
    private var itemCallback: ((Carousel) -> Unit)? = null,
    private var itemTouchCallback: (() -> Unit),
    private var infiniteSlidingCallback: (() -> Unit)
) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    private var slides: List<Carousel> = ArrayList()
    private var fakeItemCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: CarouselItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_carousel, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lastItemPosition = fakeItemCount - 1
        if (fakeItemCount > SLIDER_NUMBER_MULTIPLIER && position == lastItemPosition) {
            infiniteSlidingCallback.invoke()
        }
        val actualPosition = if (slides.size < MINIMUM_SLIDER_NUMBER) position else position % slides.size
        holder.bind(slides[actualPosition])
    }

    override fun getItemCount(): Int {
        /* Returns fake item count instead of original to make the
           carousel move automatically for a fixed time without any interruption
           or any data reloading */
        return if (slides.size < MINIMUM_SLIDER_NUMBER) slides.size else {
            fakeItemCount = SLIDER_NUMBER_MULTIPLIER * slides.size
            fakeItemCount
        }
    }

    fun getActualItemCount(): Int {
        return slides.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(slides: List<Carousel>) {
        this.slides = slides
        notifyDataSetChanged()
    }

    inner class ViewHolder (private val binding: CarouselItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: Carousel) {
            item.title?.let { binding.carouselTitle = it }
            item.subtitle?.let { binding.carouselSubTitle = it }
            binding.url = item.image
            binding.imageRequestListener = object: RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    binding.sliderImage.setImageResource(R.drawable.image_placeholder)
                    return true
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    return false
                }
            }
            binding.root.setOnClickListener {
                itemCallback?.invoke(item)
            }
            binding.root.setOnTouchListener { _, _ ->
                itemTouchCallback.invoke()
                true
            }
            binding.executePendingBindings()
        }
    }
}
