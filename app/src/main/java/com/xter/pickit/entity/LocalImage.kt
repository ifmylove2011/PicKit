package com.xter.pickit.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Author XTER
 * @Date 2021/11/26 16:17
 * @Description
 */
@Entity
data class LocalImage(@PrimaryKey val id:Long, val path:String)
