package com.xter.pickit.kit

import android.content.Context
import android.util.Log
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.bitmap_recycle.LruArrayPool
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.module.AppGlideModule


/**
 * @Author XTER
 * @Date 2021/11/30 10:04
 * @Description
 */
@GlideModule
class PhotoModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val calculator = MemorySizeCalculator.Builder(context)
            .build()
        val defaultMemoryCacheSize = calculator.memoryCacheSize
        val defaultBitmapPoolSize = calculator.bitmapPoolSize
        val defaultArrayPoolSize = calculator.arrayPoolSizeInBytes
        L.i("memory=$defaultMemoryCacheSize,bitmapPool=$defaultBitmapPoolSize,arrayPool=$defaultArrayPoolSize")
//        builder.setDefaultRequestOptions(
//            RequestOptions()
//                .format(DecodeFormat.PREFER_RGB_565)
//        )
        builder.setMemoryCache(LruResourceCache((MEMORY_SIZE).toLong()))
        builder.setBitmapPool(LruBitmapPool((BITMAP_SIZE).toLong()))
        builder.setArrayPool(LruArrayPool(ARRAY_SIZE))
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, DISK_SIZE))
        builder.setLogLevel(Log.WARN)
    }
}

const val MEMORY_SIZE = 20 * 1024 * 1024
const val BITMAP_SIZE = 10 * 1024 * 1024
const val ARRAY_SIZE = 5 * 1024 * 1024
const val DISK_SIZE = 200 * 1024 * 1024L