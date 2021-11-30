package com.xter.pickit.ui.album

import android.content.Context
import androidx.lifecycle.*
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.kit.L
import com.xter.pickit.media.IQueryResultListener
import com.xter.pickit.media.LocalMediaLoader
import kotlinx.coroutines.launch

class PhotoAlbumViewModel : ViewModel() {

    /**
     * 暂时以标记来表示
     */
    val loaded = MutableLiveData<Boolean>(false)

    /**
     * 有图片的文件夹列表
     */
    val folders: MutableLiveData<List<LocalMediaFolder>> = MutableLiveData<List<LocalMediaFolder>>()

    fun loadMediaSource(context: Context) {
        loaded.value = false
        LocalMediaLoader.INSTANCE.loadImageFolders(context,
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