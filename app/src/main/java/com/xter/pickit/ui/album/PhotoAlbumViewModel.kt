package com.xter.pickit.ui.album

import android.content.Context
import androidx.lifecycle.*
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.kit.L
import com.xter.pickit.media.IQueryResultListener
import com.xter.pickit.media.LocalMediaLoader
import kotlinx.coroutines.launch

class PhotoAlbumViewModel : ViewModel() {

    /**
     * 暂时以标记来表示
     */
    val folderLoadCompleted = MutableLiveData<Boolean>(false)
    val contentLoadCompleted = MutableLiveData<Boolean>(false)

    /**
     * 有图片的文件夹列表
     */
    val folders: MutableLiveData<List<LocalMediaFolder>> = MutableLiveData<List<LocalMediaFolder>>()

    fun loadMediaSource(context: Context) {
        folderLoadCompleted.value = false
        LocalMediaLoader.INSTANCE.loadImageFolders(context,
            object : IQueryResultListener<LocalMediaFolder> {
                override fun onCompleted(data: MutableList<LocalMediaFolder>) {
                    viewModelScope.launch {
                        folders.value = data
                        folderLoadCompleted.value = true
                    }
                    L.w("folder size=" + data.size)
                }
            })
    }

    fun loadMediaFolder(context: Context, folder: LocalMediaFolder?) {
        contentLoadCompleted.value = false
        folder?.let {
            LocalMediaLoader.INSTANCE.loadImages(
                context,
                it.bucketId,
                folder.imageNum,
                object : IQueryResultListener<LocalMedia> {
                    override fun onCompleted(data: MutableList<LocalMedia>) {
                        L.w("content size = ${data.size}")
                    }
                })
        }
    }
}