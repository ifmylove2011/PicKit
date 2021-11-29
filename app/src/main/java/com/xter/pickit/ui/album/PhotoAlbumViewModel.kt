package com.xter.pickit.ui.album

import android.content.Context
import androidx.lifecycle.*
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.kit.L
import com.xter.pickit.kit.MediaUtils
import com.xter.pickit.media.IQueryResultListener
import com.xter.pickit.media.LocalMediaLoader
import kotlinx.coroutines.launch

class PhotoAlbumViewModel : ViewModel() {

    val loaded = MutableLiveData<Boolean>(false)

    val folders: MutableLiveData<List<LocalMediaFolder>> = MutableLiveData<List<LocalMediaFolder>>()

    fun queryImage(context: Context) {
        MediaUtils.queryImages(context)
    }

    fun loadMediaSource(context: Context) {
        LocalMediaLoader.INSTANCE.loadImages(context,
            object : IQueryResultListener<LocalMediaFolder> {
                override fun onCompleted(data: MutableList<LocalMediaFolder>) {
                    viewModelScope.launch {
                        folders.value = data
                        loaded.value = true
                    }
                    L.w("folder size=" + data.size)
                }
            })
    }
}