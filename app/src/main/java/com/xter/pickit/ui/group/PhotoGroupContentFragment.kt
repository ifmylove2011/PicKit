package com.xter.pickit.ui.group

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.xter.pickit.R
import com.xter.pickit.databinding.FragmentPhotoGroupContentBinding
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.entity.LocalMediaGroup
import com.xter.pickit.ext.*
import com.xter.pickit.kit.L
import com.xter.pickit.ui.album.ContentStyle
import com.xter.pickit.ui.album.PhotoDetailActivity

/**
 * 分组图片视图，以图片为单位展示
 */
class PhotoGroupContentFragment : Fragment() {

    companion object {
        fun newInstance() = PhotoGroupContentFragment()
    }

    private lateinit var photoGroupVM: PhotoGroupViewModel
    private lateinit var photoBinding: FragmentPhotoGroupContentBinding

    private lateinit var photoContentAdapter: PhotoGroupContentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        photoGroupVM = ViewModelFactory.create(KEY_GROUP, PhotoGroupViewModel::class.java)
        photoBinding = FragmentPhotoGroupContentBinding.inflate(inflater, container, false).apply {
            this.vm = photoGroupVM
        }
        setHasOptionsMenu(true)

        return photoBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoBinding.lifecycleOwner = this.viewLifecycleOwner
        photoBinding.rvAblum.apply {
            //TODO　可动态配置的GRID、LIST
            layoutManager = GridLayoutManager(this.context, 3)
            photoBinding.vm?.let { VM ->
                photoContentAdapter = PhotoGroupContentAdapter(VM)
            }
            photoContentAdapter.setItemClickListener(object : OnGroupContentClickListener {
                override fun onItemClick(
                    groupContentFolder: GroupContentViewHolder,
                    position: Int
                ) {
                    val mediaData = groupContentFolder.binding.mediaData
                    L.i(mediaData.toString())
                    Intent(requireActivity(), PhotoDetailActivity::class.java).let { intent ->
                        intent.putParcelableArrayListExtra(
                            KEY_MEDIA_DATA,
                            photoGroupVM.images.value as ArrayList<LocalMedia>
                        )
                        intent.putExtra(KEY_MEDIA_DATA_POS, position)
                        startActivity(intent)
                    }
                }

                override fun onItemLongClick(
                    groupContentFolder: GroupContentViewHolder,
                    position: Int
                ) {
                    //长按选中，并进入多选状态，已被adapter前置实现
                }

            })
            adapter = photoContentAdapter
        }
        photoGroupVM.dataLoadCompleted.observe(viewLifecycleOwner,
            {
                L.i("group content loaded = $it")
                if (it) {
                    photoContentAdapter.submitList(photoGroupVM.images.value)
                    photoContentAdapter.notifyDataSetChanged()
                }
            })
        arguments?.getParcelable<LocalMediaGroup>(KEY_GROUP)?.let { group ->
            (activity as AppCompatActivity).supportActionBar?.let { toolbar ->
                toolbar.title = group.name
            }
            photoGroupVM.currentGroup.value = group
            photoGroupVM.loadGroupMediaData(group)
        }

        photoGroupVM.selectNum.value = 0
        photoGroupVM.selectNum.observe(viewLifecycleOwner, { selectNum ->
            (activity as AppCompatActivity).supportActionBar?.let { toolbar ->
                if (selectNum > 0) {
                    if (toolbar.title.toString().contains("(")) {
                        toolbar.title = toolbar.title?.let {
                            val title = it.substring(0, it.indexOf("("))
                            "$title($selectNum)"
                        }
                    } else {
                        toolbar.title = "${toolbar.title}($selectNum)"
                    }
                } else {
                    if (toolbar.title.toString().contains("(")) {
                        toolbar.title = toolbar.title?.let {
                            val title = it.substring(0, it.indexOf("("))
                            title
                        }
                    }
                }
            }
        })

        //监听选择视图的显示与隐藏
        photoGroupVM.choiceModeOpenForContent.observe(viewLifecycleOwner, { open ->
            photoContentAdapter.notifyDataSetChanged()
            if (!open) {
                photoGroupVM.selectNum.value = 0
            }
        })

//        photoGroupVM.pickingGroupData.observe(viewLifecycleOwner, { data ->
//            L.i("pick result = ${data.size}")
//            photoContentAdapter.submitList(data.toList())
//            photoContentAdapter.notifyDataSetChanged()
//        })

        //监听返回键
        getView()?.apply {
            isFocusableInTouchMode = true
            setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP && photoGroupVM.choiceModeOpenForContent.value == true) {
                        photoGroupVM.choiceModeOpenForContent.value = false
                        return true
                    }
                    return false
                }

            })
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.group, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_create -> {
                toPickImages()
                true
            }
            R.id.action_delete -> {
                deleteDataDialog()
                true
            }
            R.id.action_layout_grid -> {
                photoContentAdapter.setContentStyle(ContentStyle.GRID)
                true
            }
            R.id.action_layout_list -> {
                photoContentAdapter.setContentStyle(ContentStyle.LIST)
                true
            }
            else -> false
        }
    }

    fun toPickImages() {
        val bundle = Bundle()
        bundle.putInt(KEY_PICK, PICK_INTERNAL)
        view?.findNavController()?.navigate(R.id.action_nav_group_content_to_nav_ablum, bundle)
    }

    fun deleteDataDialog() {
        photoContentAdapter.getSelectGroups().let { list ->
            if (list.isNotEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("删除")
                    .setMessage("是否删除选中的${list.size}项")
                    .setCancelable(false)
                    .setPositiveButton(
                        R.string.sure
                    ) { dialog, _ ->
                        photoGroupVM.deleteSelectedData(list)
                        dialog?.dismiss()
                    }
                    .setNegativeButton(
                        R.string.cancel
                    ) { dialog, _ -> dialog?.dismiss() }
                    .create()
                    .show()
            } else {
                Snackbar.make(photoBinding.rvAblum, "未选中", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        photoGroupVM.pickingGroupData.value?.clear()
        photoGroupVM.choiceModeOpenForContent.value = false
    }
}