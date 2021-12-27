package com.xter.pickit.ui.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.xter.pickit.R
import com.xter.pickit.databinding.ItemGroupCoverBinding
import com.xter.pickit.entity.LocalMediaGroup
import com.xter.pickit.ext.GlideApp
import com.xter.pickit.kit.L
import com.xter.pickit.ui.album.ItemStyle
import com.xter.pickit.ui.widget.MODE_GRID
import com.xter.pickit.ui.widget.MODE_STACK

/**
 * @Author XTER
 * @Date 2021/11/29 14:57
 * @Description
 */
class PhotoGroupAdapter(private val VM: PhotoGroupViewModel) :
    ListAdapter<LocalMediaGroup, GroupViewHolder>(GroupDiffCallback()) {

    private lateinit var onGroupClickListener: OnGroupClickListener

    private var mStyle = ItemStyle.DEFAULT

    fun setChoiceModeOpen(open: Boolean) {
        VM.choiceModeOpenForGroup.value = open
    }

    fun setItemClickListener(listener: OnGroupClickListener) {
        onGroupClickListener = listener
    }

    fun setItemStyle(style: ItemStyle) {
        if (mStyle != style) {
            mStyle = style
            L.d("style=${style}")
            VM.groupStyle.value = style
        }
    }

    fun getSelectGroups(): List<LocalMediaGroup> {
        val selectedGroups = mutableListOf<LocalMediaGroup>()
        for (lmg in currentList) {
            if (lmg.selected)
                selectedGroups.add(lmg)
        }
        return selectedGroups
    }

    override fun getItemViewType(position: Int): Int {
        return mStyle.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        L.i("viewType = $viewType")
        return GroupViewHolder.from(parent).apply {
            this.binding.apply {
                VM.groupStyle.value?.also { style ->
                    L.i("style=$style")
                    if (style == ItemStyle.GRID) {
                        this.gicGroupCover.setMode(MODE_GRID)
                        VM.gridSpanPair.value?.let { pair ->
                            this.gicGroupCover.setRow(pair.first)
                            this.gicGroupCover.setColumn(pair.second)
                            this.gicGroupCover.resetViews(pair.first * pair.second)
                        }
                        this.gicGroupCover.invalidate()
                    } else if (style == ItemStyle.STACK) {
                        this.gicGroupCover.setMode(MODE_STACK)
                        VM.stackNum.value?.let { stackNum ->
                            this.gicGroupCover.setStackNum(stackNum)
                            this.gicGroupCover.resetViews(stackNum)
                        }
                        this.gicGroupCover.invalidate()
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(groupFolder: GroupViewHolder, position: Int) {
        groupFolder.apply {
            val group = getItem(position)
            bind(VM, group)

            //先放监听，以确定数据选中状态与数量
            binding.cbSelected.setOnCheckedChangeListener { _, isChecked ->
                group.selected = isChecked
                VM.selectGroupNum.value =
                    if (isChecked) VM.selectGroupNum.value?.plus(1) else VM.selectGroupNum.value?.minus(
                        1
                    )
            }
            if (VM.choiceModeOpenForGroup.value!!) {
                binding.cbSelected.visibility = View.VISIBLE
                binding.cbSelected.isChecked = group.selected
            } else {
                binding.cbSelected.visibility = View.GONE
            }

            binding.root.let { view ->
                view.setOnClickListener {
                    if (VM.choiceModeOpenForGroup.value!!) {
                        binding.cbSelected.isChecked = !binding.cbSelected.isChecked
                    } else {
                        onGroupClickListener.onItemClick(groupFolder, groupFolder.adapterPosition)
                    }
                }
                view.setOnLongClickListener {
                    //进入多选状态
                    if (binding.cbSelected.visibility == View.GONE) {
                        group.selected = true
                        setChoiceModeOpen(true)
                    }
                    onGroupClickListener.onItemLongClick(groupFolder, groupFolder.adapterPosition)
                    true
                }
            }

            //第一次进来时，因为目录加载过的图片有缓存，且为正方形，所以显示会不同
            if (mStyle == ItemStyle.GRID||mStyle == ItemStyle.STACK) {
                groupFolder.binding.gicGroupCover.visibility = View.VISIBLE
                groupFolder.binding.ivGroupCover.visibility = View.GONE
                val images = groupFolder.binding.gicGroupCover.getImageViews()
                L.d("images = $images")
                group.coverData?.let { covers ->
                    val maxSize = covers.size
                    for ((index, iv) in images.withIndex()) {
                        GlideApp.with(itemView)
                            .load(if (index < maxSize) covers[index].path else null)
                            .transition(withCrossFade())
                            .centerCrop()
                            .placeholder(R.drawable.image_placeholder)
                            .error(R.mipmap.ic_error)
                            .into(iv)
                    }
                }
            } else if (mStyle == ItemStyle.DEFAULT) {
                groupFolder.binding.gicGroupCover.visibility = View.GONE
                groupFolder.binding.ivGroupCover.visibility = View.VISIBLE
                GlideApp.with(itemView)
                    .load(group.firstImagePath)
                    .transition(withCrossFade())
                    .centerCrop()
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.mipmap.ic_error)
                    .into(groupFolder.binding.ivGroupCover)
            }
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<LocalMediaGroup>,
        currentList: MutableList<LocalMediaGroup>
    ) {
        L.i("group data changed")
        notifyDataSetChanged()
    }
}

class GroupViewHolder private constructor(val binding: ItemGroupCoverBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(vm: PhotoGroupViewModel, item: LocalMediaGroup) {
        binding.apply {
            this.photoGroupVM = vm
            this.group = item
            this.executePendingBindings()
        }
    }

    companion object {
        fun from(parent: ViewGroup): GroupViewHolder =
            parent.let {
                val binding =
                    ItemGroupCoverBinding.inflate(LayoutInflater.from(it.context), it, false)
                GroupViewHolder(binding)
            }
    }
}

interface OnGroupClickListener {
    fun onItemClick(groupFolder: GroupViewHolder, position: Int)
    fun onItemLongClick(groupFolder: GroupViewHolder, position: Int)
}

class GroupDiffCallback : DiffUtil.ItemCallback<LocalMediaGroup>() {
    override fun areItemsTheSame(oldItem: LocalMediaGroup, newItem: LocalMediaGroup): Boolean {
        return oldItem.groupId == newItem.groupId
    }

    override fun areContentsTheSame(oldItem: LocalMediaGroup, newItem: LocalMediaGroup): Boolean {
        return oldItem == newItem
    }
}


//@BindingAdapter("items")
//fun setItems(recyclerView: RecyclerView, items: List<LocalMediaFolder>) {
//    (recyclerView.adapter as PhotoAdapter).submitList(items)
//}