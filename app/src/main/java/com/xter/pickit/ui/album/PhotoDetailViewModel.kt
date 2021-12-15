package com.xter.pickit.ui.album

import android.content.Context
import androidx.lifecycle.*
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.ext.KEY_GROUP
import com.xter.pickit.ext.ViewModelFactory
import com.xter.pickit.kit.L
import com.xter.pickit.media.IQueryResultListener
import com.xter.pickit.media.LocalMediaLoader
import com.xter.pickit.ui.group.PhotoGroupViewModel
import kotlinx.coroutines.launch


class PhotoDetailViewModel : ViewModel() {

    val images: MutableLiveData<List<LocalMedia>> = MutableLiveData<List<LocalMedia>>()

    val currentPos: MutableLiveData<Int> = MutableLiveData(0)

}