package com.xter.pickit.ui.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.xter.pickit.R
import com.xter.pickit.databinding.ItemGroupImageBinding
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.ext.GlideApp
import com.xter.pickit.kit.L
import com.xter.pickit.ui.album.ContentDiffCallback
import com.xter.pickit.ui.album.ContentStyle

/**
 * @Author XTER
 * @Date 2021/12/11 15:10
 * @Description
 */
class PhotoGroupContentAdapter(private val VM: PhotoGroupViewModel) :
    ListAdapter<LocalMedia, GroupContentViewHolder>(ContentDiffCallback()) {

    private lateinit var onGroupContentClickListener: OnGroupContentClickListener

    private var mStyle = ContentStyle.GRID

    fun setChoiceModeOpen(open: Boolean) {
        VM.choiceModeOpenForContent.value = open
    }

    fun setItemClickListener(listener: OnGroupContentClickListener) {
        onGroupContentClickListener = listener
    }

    fun setContentStyle(style: ContentStyle) {
        mStyle = style
        L.d("style=${style.toString()}")
        if (mStyle == ContentStyle.GRID) {

        } else if (mStyle == ContentStyle.LIST) {
        }
        notifyDataSetChanged()
    }

    fun getSelectGroups(): List<LocalMedia> {
        val selectedGroups = mutableListOf<LocalMedia>()
        for (lmg in currentList) {
            if (lmg.isSelected)
                selectedGroups.add(lmg)
        }
        return selectedGroups
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupContentViewHolder {
        return GroupContentViewHolder.from(parent)
    }

    override fun onBindViewHolder(groupContentFolder: GroupContentViewHolder, position: Int) {
        groupContentFolder.apply {
            val data = getItem(position)
            bind(VM, data)

            //先放监听，以确定数据选中状态与数量
            binding.cbSelected.setOnCheckedChangeListener { _, isChecked ->
                if (data.isSelected xor isChecked) {
                    VM.selectNum.value =
                        if (isChecked) VM.selectNum.value?.plus(1) else VM.selectNum.value?.minus(1)
                }
                data.isSelected = isChecked
                if (isChecked) {
                    data.lastedViewTime = System.currentTimeMillis() / 1000
                }
            }
            if (VM.choiceModeOpenForContent.value!!) {
                binding.cbSelected.visibility = View.VISIBLE
                binding.cbSelected.isChecked = data.isSelected
            } else {
                binding.cbSelected.visibility = View.GONE
            }

            binding.root.let { view ->
                view.setOnClickListener {
                    if (VM.choiceModeOpenForContent.value!!) {
                        binding.cbSelected.isChecked = !binding.cbSelected.isChecked
                    } else {
                        onGroupContentClickListener.onItemClick(
                            groupContentFolder,
                            groupContentFolder.adapterPosition
                        )
                    }
                }
                view.setOnLongClickListener {
                    //进入多选状态
                    if (binding.cbSelected.visibility == View.GONE) {
                        binding.cbSelected.isChecked = !binding.cbSelected.isChecked
                        setChoiceModeOpen(true)
                    }
                    onGroupContentClickListener.onItemLongClick(
                        groupContentFolder,
                        groupContentFolder.adapterPosition
                    )
                    true
                }
            }

            GlideApp.with(itemView)
                .load(data.path)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .error(R.mipmap.ic_error)
                .into(groupContentFolder.binding.ivGroupContent)
        }
    }
}

class GroupContentViewHolder private constructor(val binding: ItemGroupImageBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(vm: PhotoGroupViewModel, item: LocalMedia) {
        binding.apply {
            this.photoGroupVM = vm
            this.mediaData = item
            this.executePendingBindings()
        }
    }

    companion object {
        fun from(parent: ViewGroup): GroupContentViewHolder =
            parent.let {
                val binding =
                    ItemGroupImageBinding.inflate(LayoutInflater.from(it.context), it, false)
                GroupContentViewHolder(binding)
            }
    }
}

interface OnGroupContentClickListener {
    fun onItemClick(groupContentFolder: GroupContentViewHolder, position: Int)
    fun onItemLongClick(groupContentFolder: GroupContentViewHolder, position: Int)
}