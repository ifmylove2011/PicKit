package com.xter.pickit.ui.album

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.xter.pickit.R
import com.xter.pickit.databinding.ItemFolerCoverBinding
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.ext.GlideApp
import com.xter.pickit.kit.L

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
        mStyle = style
        L.d("style=${style.toString()}")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder.from(parent)
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
                    onFolderClickListener.onItemLongClick(holderFolder, holderFolder.adapterPosition)
                    true
                }
            }

            if (mStyle == ItemStyle.GRID) {
                holderFolder.binding.gicFolderCover.visibility = View.VISIBLE
                holderFolder.binding.ivFolderCover.visibility = View.GONE
                val images = holderFolder.binding.gicFolderCover.getImageViews()
                for (iv in images) {
                    GlideApp.with(itemView)
                        .load(folder.firstImagePath)
                        .transition(withCrossFade())
                        .centerCrop()
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.mipmap.ic_error)
                        .into(iv)
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
        }
    }

}

class FolderViewHolder private constructor(val binding: ItemFolerCoverBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(vm: PhotoAlbumViewModel, item: LocalMediaFolder) {
        binding.apply {
            this.photoVM = vm
            this.folder = item
            this.executePendingBindings()
        }
    }

    companion object {
        fun from(parent: ViewGroup): FolderViewHolder =
            parent.let {
                val binding =
                    ItemFolerCoverBinding.inflate(LayoutInflater.from(it.context), it, false)
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