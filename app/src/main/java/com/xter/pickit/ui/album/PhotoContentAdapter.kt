package com.xter.pickit.ui.album

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.xter.pickit.R
import com.xter.pickit.databinding.ItemImageBinding
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.ext.GlideApp
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

    /**
     * 多选是否开启
     */
    private var choiceModeOpen = false

    fun setItemClickListener(listener: OnImageClickListener) {
        onImageClickListener = listener
    }

    fun setChoiceModeOpen(open: Boolean) {
        choiceModeOpen = open
        notifyDataSetChanged()
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

            //先放监听，以确定数据选中状态与数量
            binding.cbSelected.setOnCheckedChangeListener { _, isChecked ->
                data.isSelected = isChecked
                    VM.selectNum.value =
                        if (isChecked) VM.selectNum.value?.plus(1) else VM.selectNum.value?.minus(1)
            }
            if(choiceModeOpen){
                binding.cbSelected.visibility = View.VISIBLE
                binding.cbSelected.isChecked = data.isSelected
            }else{
                binding.cbSelected.visibility = View.GONE
            }

            binding.root.let { view ->
                view.setOnClickListener {
                    if (choiceModeOpen) {
                        binding.cbSelected.isChecked = !binding.cbSelected.isChecked
                    } else {
                        onImageClickListener.onItemClick(
                            holderContent,
                            holderContent.adapterPosition
                        )
                    }
                }
                view.setOnLongClickListener {
                    //进入多选状态
                    if (binding.cbSelected.visibility == View.GONE) {
                        data.isSelected = true
                        setChoiceModeOpen(true)
                    }
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