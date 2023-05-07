package mollah.yamin.autoslidingcarousel.app

import android.app.Application
import androidx.databinding.DataBindingUtil
import mollah.yamin.autoslidingcarousel.binding.AppDataBindingComponent

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DataBindingUtil.setDefaultComponent(AppDataBindingComponent())
    }
}