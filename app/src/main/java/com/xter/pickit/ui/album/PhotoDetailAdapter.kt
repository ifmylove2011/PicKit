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
class PhotoDetailAdapter(private val VM: PhotoAlbumViewModel) :
    ListAdapter<LocalMedia, DetailViewHolder>(DetailDiffCallback()) {

    private lateinit var onImageDetailClickListener: OnImageDetailClickListener

    private var mStyle = ContentStyle.GRID

    fun setItemClickListener(listener: OnImageDetailClickListener) {
        onImageDetailClickListener = listener
    }

    fun setContentStyle(style: ContentStyle) {
        mStyle = style
        L.d("style=${style.toString()}")
        if (mStyle == ContentStyle.GRID) {

        } else if (mStyle == ContentStyle.LIST) {
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        L.v("onCreateViewHolder")
        return DetailViewHolder.from(parent)
    }

    override fun onBindViewHolder(detailViewHolder: DetailViewHolder, position: Int) {
        detailViewHolder.apply {
            val data = getItem(position)
            bind(VM, data)
            itemView.let { view ->
                view.setOnClickListener {
                    onImageDetailClickListener.onItemClick(detailViewHolder, detailViewHolder.adapterPosition)
                }
                view.setOnLongClickListener {
                    onImageDetailClickListener.onItemLongClick(
                        detailViewHolder,
                        detailViewHolder.adapterPosition
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
                .into(detailViewHolder.binding.ivAblumContent)
        }
    }

}

interface OnImageDetailClickListener {
    fun onItemClick(contentHolder: DetailViewHolder, position: Int)
    fun onItemLongClick(contentHolder: DetailViewHolder, position: Int)
}

class DetailViewHolder private constructor(val binding: ItemImageBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(vm: PhotoAlbumViewModel, item: LocalMedia) {
        binding.apply {
            this.photoVM = vm
            this.mediaData = item
            this.executePendingBindings()
        }
    }

    companion object {
        fun from(parent: ViewGroup): DetailViewHolder =
            parent.let {
                val binding =
                    ItemImageBinding.inflate(LayoutInflater.from(it.context), it, false)
                DetailViewHolder(binding)
            }
    }
}

class DetailDiffCallback : DiffUtil.ItemCallback<LocalMedia>() {
    override fun areItemsTheSame(oldItem: LocalMedia, newItem: LocalMedia): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LocalMedia, newItem: LocalMedia): Boolean {
        return oldItem.equals(newItem)
    }
}
