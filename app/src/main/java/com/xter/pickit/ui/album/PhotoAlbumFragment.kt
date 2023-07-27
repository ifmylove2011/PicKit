package com.xter.pickit.ui.album

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.xter.pickit.R
import com.xter.pickit.databinding.FragmentPhotoAlbumBinding
import com.xter.pickit.db.SPM
import com.xter.pickit.ext.*
import com.xter.pickit.kit.L
import com.xter.pickit.ui.widget.QuickItemDecoration
import pub.devrel.easypermissions.EasyPermissions

/**
 * 相册视图，以文件夹为单位展示
 */
class PhotoAlbumFragment : Fragment() {

    companion object {
        fun newInstance() = PhotoAlbumFragment()
    }

    private lateinit var photoVM: PhotoAlbumViewModel
    private lateinit var photoBinding: FragmentPhotoAlbumBinding

    private lateinit var photoFolderAdapter: PhotoAlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        photoVM = ViewModelProvider(requireActivity()).get(PhotoAlbumViewModel::class.java)
        photoVM = ViewModelFactory.create(PhotoAlbumViewModel::class.java)
        photoBinding = FragmentPhotoAlbumBinding.inflate(inflater, container, false).apply {
            this.vm = photoVM
        }
        //因为要改变menu，所以要先于创建菜单前得到
        arguments?.getInt(KEY_PICK)?.let { pick ->
            when (pick) {
                PICK_INTERNAL, PICK_EXTERNAL, PICK_EXTERNAL_MULTIPLE -> {
                    //内部应用要挑选图片
                    photoVM.pickMode.value = pick
                    photoVM.choiceModeOpenForContent.value = true
                }
                else -> {
                    photoVM.pickMode.value = PICK_NONE
                    photoVM.choiceModeOpenForContent.value = false
                }
            }
        }
        L.d("mode = " + photoVM.pickMode.value)

        setHasOptionsMenu(true)
        return photoBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoBinding.lifecycleOwner = this.viewLifecycleOwner
        photoBinding.rvAblum.apply {
            layoutManager = GridLayoutManager(this.context, 2)
            photoBinding.vm?.let { VM ->
                photoFolderAdapter = PhotoAlbumAdapter(VM)
            }
            photoFolderAdapter.setItemClickListener(object : OnFolderClickListener {
                override fun onItemClick(holderFolder: FolderViewHolder, position: Int) {
                    val folder = holderFolder.getData()
                    L.d(folder.toString())
                    val bundle = Bundle()
                    bundle.putParcelable(KEY_FOLDER, folder)
                    findNavController().navigate(R.id.action_nav_ablum_to_nav_content, bundle)
                }


                override fun onItemLongClick(holderFolder: FolderViewHolder, position: Int) {

                }

            })
            //布局参数
            checkGroupStyle()
//            adapter = photoFolderAdapter
        }
        var per = Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            per = Manifest.permission.READ_MEDIA_IMAGES
        }
        //首次得拿到权限
        if (!EasyPermissions.hasPermissions(
                requireContext(),
                per
            )
        ) {
            return
        }
        //目录数据加载监听
        photoVM.folderLoadCompleted.observe(viewLifecycleOwner,
            {
                L.i("album loaded = $it")
                if (it) {
                    photoFolderAdapter.submitList(photoVM.folders.value)
                    photoFolderAdapter.notifyDataSetChanged()
                }
            })
        //目录数据几乎是不变的，因此可以用来判断是否已经加载过
        if (photoVM.folders.value == null) {
            L.i("loading folder......")
            photoVM.loadMediaFolder(requireContext())
        }
        //监听pickMode下的选择总数，浏览情况下并不处理
        photoVM.pickingNum.observe(viewLifecycleOwner, { num ->
            L.i("picking num=$num")
            (activity as AppCompatActivity).supportActionBar?.let { toolbar ->
                if (num > 0) {
                    if (toolbar.title.toString().contains("(")) {
                        toolbar.title = toolbar.title?.let {
                            val title = it.substring(0, it.indexOf("("))
                            L.i("title=$title")
                            "$title($num)"
                        }
                    } else {
                        toolbar.title = "${toolbar.title}($num)"
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
        //布局的改变
        photoVM.groupStyle.observe(viewLifecycleOwner, { style ->
//            photoFolderAdapter.notifyDataSetChanged()
            photoBinding.rvAblum.apply {
                layoutManager = if (style == ItemStyle.LIST) {
                    addItemDecoration(
                        QuickItemDecoration(
                            requireContext(),
                            LinearLayoutManager.VERTICAL
                        )
                    )
                    LinearLayoutManager(this.context)
                } else {
                    GridLayoutManager(this.context, 2)
                }
                adapter = photoFolderAdapter
            }
        })
    }

    fun checkGroupStyle() {
        L.i("check style")
        val style = SPM.getStr(this.context, CONFIG, KEY_FOLDER, ItemStyle.DEFAULT.name)
        if (style == ItemStyle.GRID.name) {
            val row = SPM.getStr(this.context, CONFIG, GRID_ROW, "2").toInt()
            val column = SPM.getStr(this.context, CONFIG, GRID_COLUMN, "2").toInt()
            photoVM.gridSpanPair.value = Pair(row, column)
        } else if (style == ItemStyle.STACK.name) {
            photoVM.stackNum.value = SPM.getStr(this.context, CONFIG, STACK_NUM, "3").toInt()
        }
        photoFolderAdapter.setItemStyle(ItemStyle.valueOf(style))
    }

    fun createDetailFragment() {
        val detailFragment = PhotoDetailFragment()
        parentFragmentManager.beginTransaction()?.let { ft ->
            ft.add(detailFragment, TAG_DETAIL)
            ft.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        L.d("mode = " + photoVM.pickMode.value)
        if (photoVM.pickMode.value != PICK_NONE) {
            inflater.inflate(R.menu.album_pick, menu)
        } else {
            inflater.inflate(R.menu.album, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sure -> {
                photoVM.pickFinish()
                findNavController().popBackStack()
                true
            }
//            R.id.action_cancel->{
//                findNavController().popBackStack()
//                true
//            }
            R.id.action_layout_grid -> {
                SPM.saveStr(context, CONFIG, KEY_FOLDER, ItemStyle.GRID.name)
                photoFolderAdapter.setItemStyle(ItemStyle.GRID)
                true
            }
            R.id.action_layout_default -> {
                SPM.saveStr(context, CONFIG, KEY_FOLDER, ItemStyle.DEFAULT.name)
                photoFolderAdapter.setItemStyle(ItemStyle.DEFAULT)
                true
            }
            R.id.action_layout_stack -> {
                SPM.saveStr(context, CONFIG, KEY_FOLDER, ItemStyle.STACK.name)
                photoFolderAdapter.setItemStyle(ItemStyle.STACK)
                true
            }
            R.id.action_layout_list -> {
                SPM.saveStr(context, CONFIG, KEY_FOLDER, ItemStyle.LIST.name)
                photoFolderAdapter.setItemStyle(ItemStyle.LIST)
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}