package com.xter.pickit.ext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.xter.pickit.ui.album.PhotoAlbumViewModel
import com.xter.pickit.ui.album.PhotoDetailViewModel
import com.xter.pickit.ui.group.PhotoGroupViewModel


/**
 * @Author XTER
 * @Date 2021/12/6 10:03
 * @Description
 */
const val DEFAULT_KEY = "APPLICATION"
const val KEY_FOLDER = "FOLDER"
const val KEY_GROUP = "GROUP"
const val KEY_DETAIL = "DETAIL"

const val KEY_PICK = "pick"
const val KEY_MEDIA_DATA = "mediaData"
const val KEY_MEDIA_DATA_POS = "mediaDataPos"

const val PICK_NONE = -1
const val PICK_INTERNAL = 0
const val PICK_EXTERNAL = 1
const val PICK_EXTERNAL_MULTIPLE = 2

const val TAG_DETAIL = "detail"

const val CONFIG = "config"
const val GRID_ROW = "grid_row"
const val GRID_COLUMN = "grid_column"
const val STACK_NUM = "stack_num"

object ViewModelFactory : ViewModelProvider.Factory {

    private val vmMap = HashMap<String, ViewModel?>()

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
                    it.isAssignableFrom(PhotoDetailViewModel::class.java) -> PhotoDetailViewModel()
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