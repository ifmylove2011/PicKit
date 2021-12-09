package com.xter.pickit.ext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.xter.pickit.ui.album.PhotoAlbumViewModel
import com.xter.pickit.ui.group.PhotoGroupViewModel


/**
 * @Author XTER
 * @Date 2021/12/6 10:03
 * @Description
 */
const val DEFAULT_KEY = "APPLICATION"
const val FOLDER_KEY = "FOLDER"
const val GROUP_KEY = "GROUP"

object ViewModelFactory : ViewModelProvider.Factory {

    val vmMap = HashMap<String, ViewModel?>()

    fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>
    ): T {
        var vm = vmMap.get(key)
        if (vm == null) {
            vm = modelClass.let {
                when {
                    it.isAssignableFrom(PhotoAlbumViewModel::class.java) -> PhotoAlbumViewModel()
                    it.isAssignableFrom(PhotoGroupViewModel::class.java) -> PhotoGroupViewModel()
                    else ->
                        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
            vmMap[key] = vm
        }
        return vm as T
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return create(DEFAULT_KEY,modelClass)
    }

}