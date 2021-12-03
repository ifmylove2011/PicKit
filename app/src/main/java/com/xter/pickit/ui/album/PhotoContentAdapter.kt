package com.xter.pickit.ui.album

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.xter.pickit.R
import com.xter.pickit.databinding.ItemImageBinding
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.kit.GlideApp
import com.xter.pickit.kit.L

/**
 * @Author XTER
 * @Date 2021/11/29 14:57
 * @Description
 */
class PhotoContentAdapter(private val VM: PhotoAlbumViewModel) :
    ListAdapter<LocalMedia, ContentViewHolder>(ContentDiffCallback()) {

    private lateinit var onImageClickListener: OnImageClickListener

    private var mStyle = ContentStyle.GRID

    fun setItemClickListener(listener: OnImageClickListener) {
        onImageClickListener = listener
    }

    fun setContentStyle(style: ContentStyle) {
        mStyle = style
        L.d("style=${style.toString()}")
        if (mStyle == ContentStyle.GRID) {

        } else if (mStyle == ContentStyle.LIST) {
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        return ContentViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderContent: ContentViewHolder, position: Int) {
        holderContent.apply {
            val data = getItem(position)
            bind(VM, data)
            itemView.let { view ->
                view.setOnClickListener {
                    onImageClickListener.onItemClick(holderContent, holderContent.adapterPosition)
                }
                view.setOnLongClickListener {
                    onImageClickListener.onItemLongClick(
                        holderContent,
                        holderContent.adapterPosition
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
                .into(holderContent.binding.ivAblumContent)
        }
    }

}

interface OnImageClickListener {
    fun onItemClick(contentHolder: ContentViewHolder, position: Int)
    fun onItemLongClick(contentHolder: ContentViewHolder, position: Int)
}

class ContentViewHolder private constructor(val binding: ItemImageBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(vm: PhotoAlbumViewModel, item: LocalMedia) {
        binding.apply {
            this.photoVM = vm
            this.mediaData = item
            this.executePendingBindings()
        }
    }

    companion object {
        fun from(parent: ViewGroup): ContentViewHolder =
            parent.let {
                val binding =
                    ItemImageBinding.inflate(LayoutInflater.from(it.context), it, false)
                ContentViewHolder(binding)
            }
    }
}

class ContentDiffCallback : DiffUtil.ItemCallback<LocalMedia>() {
    override fun areItemsTheSame(oldItem: LocalMedia, newItem: LocalMedia): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LocalMedia, newItem: LocalMedia): Boolean {
        return oldItem.equals(newItem)
    }
}

enum class ContentStyle {
    GRID,
    LIST
}

//@BindingAdapter("items")
//fun setItems(recyclerView: RecyclerView, items: List<LocalMediaFolder>) {
//    (recyclerView.adapter as PhotoAdapter).submitList(items)
//}