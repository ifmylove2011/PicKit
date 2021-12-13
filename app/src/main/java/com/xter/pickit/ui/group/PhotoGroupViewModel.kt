package com.xter.pickit.ui.group

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xter.pickit.db.RoomDBM
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.entity.LocalMediaGroup
import com.xter.pickit.kit.L
import kotlinx.coroutines.launch

/**
 * @Author XTER
 * @Date 2021/12/9 9:28
 * @Description
 */

const val KEY_GROUP = "group"
const val KEY_PICK = "pick"

class PhotoGroupViewModel : ViewModel() {

    /**
     * 暂时以标记来表示
     */
    val groupLoadCompleted = MutableLiveData<Boolean>(false)
    val dataLoadCompleted = MutableLiveData<Boolean>(false)
    val choiceModeOpenForGroup = MutableLiveData<Boolean>(false)

    /**
     * 有图片的分组列表
     */
    val groups: MutableLiveData<List<LocalMediaGroup>> = MutableLiveData<List<LocalMediaGroup>>()
    val images: MutableLiveData<List<LocalMedia>> = MutableLiveData<List<LocalMedia>>()

    val selectGroupNum:MutableLiveData<Int> = MutableLiveData(0)
    val selectNum:MutableLiveData<Int> = MutableLiveData(0)

    val picking:MutableLiveData<LocalMediaGroup> = MutableLiveData(null)
    val pickingGroupData:MutableLiveData<List<LocalMedia>> = MutableLiveData<List<LocalMedia>>()
    val pickingNum:MutableLiveData<Int> = MutableLiveData(0)

    fun createNewGroup(groupName: String?) {
        viewModelScope.launch {
            LocalMediaGroup(0L, groupName, "", "", 0).let { group ->
                val id = RoomDBM.get().insertGroup(group)
                if (id != 0L) {
                    if (TextUtils.isEmpty(groupName)) {
                        group.groupId = id
                        group.name = "默认$id"
                        val row = RoomDBM.get().updateGroup(group)
                    }
                    loadGroups()
                }
            }
        }
    }

    fun deleteGroups(groups:List<LocalMediaGroup>){
        viewModelScope.launch {
            RoomDBM.get().deleteGroups(groups)?.let { rows->
                if(rows>0){
                    L.i("删除group $rows")
                    selectGroupNum.value = 0
                    choiceModeOpenForGroup.value = false
                    loadGroups()
                }
            }
        }
    }


    fun loadGroups() {
        viewModelScope.launch {
            groupLoadCompleted.value = false
            val data = RoomDBM.get().queryGroup()
            groups.value = data
            groupLoadCompleted.value = true
        }
    }

    fun loadGroupMediaData(context: Context, group:LocalMediaGroup?){

    }
}