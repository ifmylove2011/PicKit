package com.xter.pickit.ui.album

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.xter.pickit.R
import com.xter.pickit.databinding.ItemFolderCoverBinding
import com.xter.pickit.databinding.ItemFolderCoverBindingImpl
import com.xter.pickit.databinding.ItemFolderCoverListBinding
import com.xter.pickit.databinding.ItemFolderCoverListBindingImpl
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.ext.GlideApp
import com.xter.pickit.kit.L
import com.xter.pickit.ui.widget.MODE_GRID
import com.xter.pickit.ui.widget.MODE_STACK

/**
 * @Author XTER
 * @Date 2021/11/29 14:57
 * @Description
 */
class PhotoAlbumAdapter(private val VM: PhotoAlbumViewModel) :
    ListAdapter<LocalMediaFolder, FolderViewHolder>(FolderDiffCallback()) {

    private lateinit var onFolderClickListener: OnFolderClickListener

    private var mStyle = ItemStyle.DEFAULT

    fun setItemClickListener(listener: OnFolderClickListener) {
        onFolderClickListener = listener
    }

    fun setItemStyle(style: ItemStyle) {
        if (mStyle != style) {
            mStyle = style
            L.d("style=${style}")
            VM.groupStyle.value = style
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mStyle.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder.from(parent, viewType).apply {
            this.binding.apply {
                if (this is ItemFolderCoverBinding) {
                    VM.groupStyle.value?.also { style ->
//                        L.i("style=$style")
                        if (style == ItemStyle.GRID) {
                            this.gicFolderCover.setMode(MODE_GRID)
                            VM.gridSpanPair.value?.let { pair ->
                                this.gicFolderCover.resetViews(pair.first * pair.second)
                                this.gicFolderCover.setRow(pair.first)
                                this.gicFolderCover.setColumn(pair.second)
                            }
                            this.gicFolderCover.invalidate()
                        } else if (style == ItemStyle.STACK) {
                            this.gicFolderCover.setMode(MODE_STACK)
                            VM.stackNum.value?.let { stackNum ->
                                this.gicFolderCover.resetViews(stackNum)
                                this.gicFolderCover.setStackNum(stackNum)
                            }
                            this.gicFolderCover.invalidate()
                        }
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holderFolder: FolderViewHolder, position: Int) {
        holderFolder.apply {
            val folder = getItem(position)
            bind(VM, folder)
            itemView.let { view ->
                view.setOnClickListener {
                    onFolderClickListener.onItemClick(holderFolder, holderFolder.adapterPosition)
                }
                view.setOnLongClickListener {
                    onFolderClickListener.onItemLongClick(
                        holderFolder,
                        holderFolder.adapterPosition
                    )
                    true
                }
            }
            if (holderFolder.binding is ItemFolderCoverBinding) {
                if (mStyle == ItemStyle.GRID || mStyle == ItemStyle.STACK) {
                    holderFolder.binding.gicFolderCover.visibility = View.VISIBLE
                    holderFolder.binding.ivFolderCover.visibility = View.GONE
                    val images = holderFolder.binding.gicFolderCover.getImageViews()
//                L.d("images = $images")
                    folder.data?.let { covers ->
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
                    holderFolder.binding.gicFolderCover.visibility = View.GONE
                    holderFolder.binding.ivFolderCover.visibility = View.VISIBLE
                    GlideApp.with(itemView)
                        .load(folder.firstImagePath)
                        .transition(withCrossFade())
                        .centerCrop()
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.mipmap.ic_error)
                        .into(holderFolder.binding.ivFolderCover)
                }
            } else if (holderFolder.binding is ItemFolderCoverListBinding) {
                GlideApp.with(itemView)
                    .load(folder.firstImagePath)
                    .transition(withCrossFade())
                    .centerCrop()
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.mipmap.ic_error)
                    .into(holderFolder.binding.ivFolderCover)
            }
        }

    }

    override fun onCurrentListChanged(
        previousList: MutableList<LocalMediaFolder>,
        currentList: MutableList<LocalMediaFolder>
    ) {
        notifyDataSetChanged()
    }
}

class FolderViewHolder private constructor(val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(vm: PhotoAlbumViewModel, item: LocalMediaFolder) {
        if (binding is ItemFolderCoverBinding) {
            binding.apply {
                this.photoVM = vm
                this.folder = item
                this.executePendingBindings()
            }
        } else if (binding is ItemFolderCoverListBinding) {
            binding.apply {
                this.photoVM = vm
                this.folder = item
                this.executePendingBindings()
            }
        }
    }

    fun getData(): LocalMediaFolder? {
        return when (binding) {
            is ItemFolderCoverBindingImpl -> {
                binding.folder
            }
            is ItemFolderCoverListBindingImpl -> {
                binding.folder
            }
            else -> {
                null
            }
        }
    }

    companion object {
        fun from(parent: ViewGroup, viewType: Int): FolderViewHolder =
            parent.let {
                val binding =
                    if (viewType == ItemStyle.LIST.ordinal) ItemFolderCoverListBinding.inflate(
                        LayoutInflater.from(it.context),
                        it,
                        false
                    )
                    else ItemFolderCoverBinding.inflate(LayoutInflater.from(it.context), it, false)
                FolderViewHolder(binding)
            }
    }
}

interface OnFolderClickListener {
    fun onItemClick(holderFolder: FolderViewHolder, position: Int)
    fun onItemLongClick(holderFolder: FolderViewHolder, position: Int)
}

class FolderDiffCallback : DiffUtil.ItemCallback<LocalMediaFolder>() {
    override fun areItemsTheSame(oldItem: LocalMediaFolder, newItem: LocalMediaFolder): Boolean {
        return oldItem.bucketId == newItem.bucketId
    }

    override fun areContentsTheSame(oldItem: LocalMediaFolder, newItem: LocalMediaFolder): Boolean {
        return oldItem.equals(newItem)
    }
}


enum class ItemStyle {
    DEFAULT,
    GRID,
    STACK,
    LIST
}

//@BindingAdapter("items")
//fun setItems(recyclerView: RecyclerView, items: List<LocalMediaFolder>) {
//    (recyclerView.adapter as PhotoAdapter).submitList(items)
//}