package com.exercise.app

import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder

object CoilConfig {

    lateinit var staticLoader: ImageLoader
        private set

    fun init(context: Context) {
        val animatedLoader = ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .crossfade(true)
            .build()
        Coil.setImageLoader(animatedLoader)

        staticLoader = ImageLoader.Builder(context)
            .crossfade(true)
            .build()
    }
}