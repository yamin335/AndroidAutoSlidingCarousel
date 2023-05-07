package mollah.yamin.autoslidingcarousel.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mollah.yamin.autoslidingcarousel.R
import mollah.yamin.autoslidingcarousel.databinding.MainActivityBinding
import mollah.yamin.autoslidingcarousel.models.Carousel
import mollah.yamin.autoslidingcarousel.ui.adapters.CarouselAdapter
import mollah.yamin.autoslidingcarousel.ui.adapters.CarouselIndicatorAdapter
import mollah.yamin.autoslidingcarousel.utils.AppConstants
import mollah.yamin.autoslidingcarousel.utils.AppConstants.MINIMUM_SLIDER_NUMBER
import mollah.yamin.autoslidingcarousel.utils.AppConstants.SLIDER_CHANGE_TIME_INTERVAL
import mollah.yamin.autoslidingcarousel.utils.AppConstants.SLIDER_HUMAN_INTERACTION_AWAIT_TIME
import mollah.yamin.autoslidingcarousel.utils.AppConstants.START_TIME_IN_MILLI_SECONDS
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var indicatorAdapter: CarouselIndicatorAdapter
    private lateinit var carouselPageChangeCallback: CarouselPageChangeCallback
    private lateinit var carouselAdapter: CarouselAdapter

    private var countdownTimer: CountDownTimer? = null

    private var firstTime = true
    private var isHumanInteracting = false
    private var sliderAutoScrollAwaitJobScope = CoroutineScope(Job() + Dispatchers.Main.immediate)
    
    override fun onResume() {
        super.onResume()

        if (!firstTime) {
            startTimer()
        }
        firstTime = false
    }

    override fun onPause() {
        super.onPause()
        resetTimer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carouselAdapter = CarouselAdapter({
            // handle carousel item click listener here
        }, {
            // Tracing user interaction
            isHumanInteracting = true
        }) {
            // Reaches the end of carousel, so re-generate the carousels
            processSlides(listOfCarousel)
        }

        carouselPageChangeCallback = CarouselPageChangeCallback {
            detectUserInterAction()
            indicatorAdapter.setIndicatorAt(it)
        }

        binding.sliderView.apply {
            // Set offscreen page limit to at least 1, so adjacent pages are always laid out
            offscreenPageLimit = 1
            val recyclerView = getChildAt(0) as RecyclerView
            recyclerView.apply {
                val padding = resources.getDimensionPixelOffset(R.dimen.halfPageMargin) +
                        resources.getDimensionPixelOffset(R.dimen.peekOffset)
                // setting padding on inner RecyclerView puts overscroll effect in the right place
                setPadding(padding, 0, padding, 0)
                clipToPadding = false
            }
            adapter = carouselAdapter
            registerOnPageChangeCallback(carouselPageChangeCallback)
            setPageTransformer(sliderAnimator)
        }

        indicatorAdapter = CarouselIndicatorAdapter { itemPosition ->
            isHumanInteracting = true
            detectUserInterAction()
            binding.sliderView.post {
                if (carouselAdapter.getActualItemCount() >= MINIMUM_SLIDER_NUMBER)  {
                    val currentPosition = binding.sliderView.currentItem
                    val factor = currentPosition / carouselAdapter.getActualItemCount()
                    val nextPosition = (factor * carouselAdapter.getActualItemCount()) + itemPosition
                    binding.sliderView.setCurrentItem(nextPosition, true)
                } else {
                    binding.sliderView.setCurrentItem(itemPosition, true)
                }
            }
        }
        binding.indicatorView.adapter = indicatorAdapter

        processSlides(listOfCarousel)
        lifecycleScope.launch {
            delay(SLIDER_CHANGE_TIME_INTERVAL)
            startTimer()
        }
    }

    private fun detectUserInterAction() {
        if (isHumanInteracting) {
            resetTimer()
            sliderAutoScrollAwaitJobScope.cancel()
            sliderAutoScrollAwaitJobScope = CoroutineScope(Job() + Dispatchers.Main.immediate)
            val sliderAutoScrollAwaitJob = sliderAutoScrollAwaitJobScope.launch {
                delay(SLIDER_HUMAN_INTERACTION_AWAIT_TIME)
                startTimer()
            }

            lifecycleScope.launch {
                sliderAutoScrollAwaitJob.join()
            }
        }
    }

    private fun processSlides(banners: List<Carousel>) {
        binding.sliderView.post {
            carouselAdapter.submitList(banners)
            indicatorAdapter.setNumberOfSlides(banners.size)
            if (banners.size >= MINIMUM_SLIDER_NUMBER) {
                var position = banners.size * AppConstants.SLIDER_NUMBER_MULTIPLIER
                position /= 2
                binding.sliderView.setCurrentItem(position, false)
                val actualPosition = if (carouselAdapter.getActualItemCount() < MINIMUM_SLIDER_NUMBER) position else position % carouselAdapter.getActualItemCount()
                indicatorAdapter.setIndicatorAt(actualPosition)
            }
        }
    }

    private fun startTimer() {
        resetTimer()
        countdownTimer = object : CountDownTimer(START_TIME_IN_MILLI_SECONDS, SLIDER_CHANGE_TIME_INTERVAL) {
            override fun onFinish() {
                startTimer()
            }

            override fun onTick(time_in_milli_seconds: Long) {
                var currentPosition = binding.sliderView.currentItem
                binding.sliderView.post {
                    binding.sliderView.setCurrentItem(++currentPosition, true)
                }
            }
        }
        countdownTimer?.start()
    }

    private fun resetTimer() {
        isHumanInteracting = false
        countdownTimer?.cancel()
        countdownTimer = null
    }

    inner class CarouselPageChangeCallback(private val listener: (Int) -> Unit) : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            val actualPosition = if (carouselAdapter.getActualItemCount() < MINIMUM_SLIDER_NUMBER) position else position % carouselAdapter.getActualItemCount()
            listener.invoke(actualPosition)
        }
    }
    
    companion object {
        val listOfCarousel = listOf(
            Carousel(1, "https://media.istockphoto.com/id/1285606275/vector/cesarean-section-set.jpg?s=612x612&w=is&k=20&c=AxgnvaI7NVeojF6tWyaIui_fNJYjLjNnPcldb9Ikmd4=",
                "Caesarean Delivery Problem", "Now-a-days 90% delivery are being Caesarean"),
            Carousel(2, "https://media.istockphoto.com/id/1425147156/vector/cesarean-section.jpg?s=612x612&w=is&k=20&c=_ZbT1BlA0U8Drs4V7sAmr5wB6i6lE0PaASg3X0FlQpQ=",
                "Newborn Mortality Problem", "Approximately 43% of pneumonia-related deaths occur in age 1-11 months"),
            Carousel(3, "https://media.istockphoto.com/id/1042843762/vector/childbirth.jpg?s=1024x1024&w=is&k=20&c=jgKrGW8mo85mLByl_zsc7CCOVj1SgR3oV3FHIDnsfAA=",
                "Unskilled Birth Attendant", "The major complications can occur due to unskilled attendants.")
        )

        val sliderAnimator = ViewPager2.PageTransformer { page, position ->
//        val scaleFactor = 0.8f.coerceAtLeast(1 - abs(position - 0.01f))
//        page.scaleY = scaleFactor

            page.apply {
                val minScale = 0.85f
                val maxScale = 0.6f

//            val pageWidth = width
//            val pageHeight = height
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = maxScale
                        val scaleFactor = minScale.coerceAtLeast(1 - abs(position))
                        scaleY = scaleFactor
                    }
                    position <= 1 -> { // [-1,1]
                        // Modify the default slide transition to shrink the page as well
                        val scaleFactor = minScale.coerceAtLeast(1 - abs(position))
                        //val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = resources.getDimensionPixelOffset(R.dimen.halfPageMargin)
                        translationX = horzMargin.toFloat()/2

//                    translationX = if (position < 0) {
//                        horzMargin - vertMargin / 2
//                    } else {
//                        horzMargin + vertMargin / 2
//                    }

                        // Scale the page down (between MIN_SCALE and 1)
                        //scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (maxScale + (((scaleFactor - minScale) / (1 - minScale)) * (1 - maxScale)))
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = maxScale
                        val scaleFactor = minScale.coerceAtLeast(1 - abs(position))
                        scaleY = scaleFactor
                    }
                }
            }
        }
    }
}