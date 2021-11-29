package com.xter.pickit.ui.album

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xter.pickit.databinding.ItemFolerCoverBinding
import com.xter.pickit.entity.LocalMediaFolder

/**
 * @Author XTER
 * @Date 2021/11/29 14:57
 * @Description
 */
class PhotoAdapter(private val VM: PhotoAlbumViewModel) :
    ListAdapter<LocalMediaFolder, ViewHolderK>(FolderDiffCallback()) {
    private lateinit var onItemClickListener: OnItemClickListener

    fun setItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderK {
        return ViewHolderK.from(parent)
    }



    override fun onBindViewHolder(holder: ViewHolderK, position: Int) {
        holder.apply {
            val folder = getItem(position)
            bind(VM,folder)
            itemView.let { view->
                view.setOnClickListener {
                    onItemClickListener.onItemClick(holder, holder.adapterPosition)
                }
                view.setOnLongClickListener {
                    onItemClickListener.onItemLongClick(holder, holder.adapterPosition)
                    true
                }
            }
        }
    }

}

class ViewHolderK private constructor(val binding: ItemFolerCoverBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(vm: PhotoAlbumViewModel, item: LocalMediaFolder) {
        binding.apply {
            this.photoVM = vm
            this.folder = item
            this.executePendingBindings()
        }
    }

    companion object {
        fun from(parent: ViewGroup): ViewHolderK =
            parent.let {
                val binding =
                    ItemFolerCoverBinding.inflate(LayoutInflater.from(it.context), it, false)
                ViewHolderK(binding)
            }
    }
}

interface OnItemClickListener {
    fun onItemClick(holderK: ViewHolderK, position: Int)
    fun onItemLongClick(holderK: ViewHolderK, position: Int)
}

class FolderDiffCallback : DiffUtil.ItemCallback<LocalMediaFolder>() {
    override fun areItemsTheSame(oldItem: LocalMediaFolder, newItem: LocalMediaFolder): Boolean {
        return oldItem.bucketId == newItem.bucketId
    }

    override fun areContentsTheSame(oldItem: LocalMediaFolder, newItem: LocalMediaFolder): Boolean {
        return oldItem.equals(newItem)
    }
}

//@BindingAdapter("items")
//fun setItems(recyclerView: RecyclerView, items: List<LocalMediaFolder>) {
//    (recyclerView.adapter as PhotoAdapter).submitList(items)
//}