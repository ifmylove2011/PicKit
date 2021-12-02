package com.xter.pickit.ui.album

import android.content.Context
import androidx.lifecycle.*
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.kit.L
import com.xter.pickit.media.IQueryResultListener
import com.xter.pickit.media.LocalMediaLoader
import kotlinx.coroutines.launch

const val KEY_FOLDER = "folder"
const val KEY_MEDIA_DATA = "mediaData"

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
    val images: MutableLiveData<List<LocalMedia>> = MutableLiveData<List<LocalMedia>>()

    /**
     * 查找所有图片
     */
    fun loadMediaFolder(context: Context) {
        viewModelScope.launch {
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
    }

    /**
     * 根据目录加载图片列表
     */
    fun loadMediaData(context: Context, folder: LocalMediaFolder?) {
        viewModelScope.launch {
            contentLoadCompleted.value = false
            folder?.let {
                LocalMediaLoader.INSTANCE.loadImages(
                    context,
                    it.bucketId,
                    folder.imageNum,
                    object : IQueryResultListener<LocalMedia> {
                        override fun onCompleted(data: MutableList<LocalMedia>) {
//                            CoroutineScope(Dispatchers.Main).launch {//另一种主线程写法，其实viewModelScope也在主线程，不过绑定了lifecycle更可靠
                            viewModelScope.launch {
                                images.value = data
                                contentLoadCompleted.value = true
                            }
                            L.w("image num = ${data.size}")
                        }
                    })
            }
        }
    }
}