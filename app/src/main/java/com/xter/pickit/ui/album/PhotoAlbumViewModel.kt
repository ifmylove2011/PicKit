package com.xter.pickit.ui.album

import android.content.Context
import androidx.lifecycle.ViewModel
import com.xter.pickit.kit.MediaUtils

class PhotoAlbumViewModel : ViewModel() {

    fun queryImage(context: Context){
        MediaUtils.queryImages(context)
    }
}