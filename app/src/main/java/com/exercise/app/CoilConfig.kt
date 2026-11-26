package com.exercise.app

import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.memory.MemoryCache
import coil.request.ImageRequest

/**
 * Coil 图片加载器配置。
 *
 * 这里有意识地为「列表静态图」和「详情动图」保留两个 loader：
 * - 列表不注册 GifDecoder，只解首帧，滚动时不产生动画开销；
 * - 两个 loader 使用**各自**的内存缓存。共用同一份缓存看似省内存，但缓存键只由
 *   数据与尺寸决定，静态帧与动图会命中同一个键，导致详情页拿到列表缓存的静止帧。
 *   因此改为各自独立的缓存，并显式限制上限（合计 25%，与 Coil 单个默认缓存相当），
 *   既避免重复解码，也不会让缓存占用翻倍。
 *
 * 初始化放在 [ExerciseApplication] 中执行一次，避免 Activity 重建时反复新建 loader 与缓存。
 */
object CoilConfig {

    private const val STATIC_CACHE_PERCENT = 0.15
    private const val ANIMATED_CACHE_PERCENT = 0.10

    @Volatile
    private var initialized = false

    private var staticImageLoader: ImageLoader? = null

    /** 列表用：不注册 GifDecoder，只解首帧，显著降低内存占用 */
    val staticLoader: ImageLoader
        get() = checkNotNull(staticImageLoader) {
            "CoilConfig.init() 尚未调用，请检查 ExerciseApplication"
        }

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return

            val appContext = context.applicationContext

            val animatedLoader = ImageLoader.Builder(appContext)
                .components { add(GifDecoder.Factory()) }
                .memoryCache(
                    MemoryCache.Builder(appContext)
                        .maxSizePercent(ANIMATED_CACHE_PERCENT)
                        .build()
                )
                .crossfade(true)
                .build()

            Coil.setImageLoader(animatedLoader)

            staticImageLoader = ImageLoader.Builder(appContext)
                .memoryCache(
                    MemoryCache.Builder(appContext)
                        .maxSizePercent(STATIC_CACHE_PERCENT)
                        .build()
                )
                .crossfade(true)
                .build()

            initialized = true
        }
    }

    /** 统一的本地资源图片请求，避免路径拼接散落在各个 Composable 里 */
    fun assetRequest(context: Context, fileName: String): ImageRequest =
        ImageRequest.Builder(context.applicationContext)
            .data("file:///android_asset/images/$fileName")
            .crossfade(true)
            .build()
}
