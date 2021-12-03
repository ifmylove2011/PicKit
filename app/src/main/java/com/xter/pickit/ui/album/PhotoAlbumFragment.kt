package com.xter.pickit.ui.album

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.xter.pickit.R
import com.xter.pickit.databinding.FragmentPhotoAlbumBinding
import com.xter.pickit.kit.L

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
        photoVM = ViewModelProvider(requireActivity()).get(PhotoAlbumViewModel::class.java)
        photoBinding = FragmentPhotoAlbumBinding.inflate(inflater, container, false).apply {
            this.vm = photoVM
        }
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.show()
        L.d("backStackEntryCount=${this.fragmentManager?.backStackEntryCount}")
        L.d("fragmentSize=${this.fragmentManager?.fragments?.size}")
        return photoBinding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        photoBinding.lifecycleOwner = this.viewLifecycleOwner
        photoBinding.rvAblum.apply {
            layoutManager = GridLayoutManager(this.context, 2)
            photoBinding.vm?.let { VM ->
                photoFolderAdapter = PhotoAlbumAdapter(VM)
            }
            photoFolderAdapter.setItemClickListener(object : OnFolderClickListener {
                override fun onItemClick(holderFolder: FolderViewHolder, position: Int) {
                    val folder = holderFolder.binding.folder
                    L.d(folder.toString())
                    val bundle = Bundle()
                    bundle.putParcelable(KEY_FOLDER, folder)
                    findNavController().navigate(R.id.action_nav_ablum_to_nav_content, bundle)
                }


                override fun onItemLongClick(holderFolder: FolderViewHolder, position: Int) {

                }

            })
            adapter = photoFolderAdapter
        }
        photoVM.folderLoadCompleted.observe(viewLifecycleOwner,
            {
                L.i("loaded = $it")
                if (it) {
                    photoFolderAdapter.submitList(photoVM.folders.value)
                    photoFolderAdapter.notifyDataSetChanged()
                }
            })
        //目录数据几乎是不变的，因此可以用来判断是否已经加载过
        if (photoVM.folders.value == null) {
            photoVM.loadMediaFolder(requireContext())
        }
    }

    fun createDetailFragment() {
        val detailFragment = PhotoDetailFragment()
        fragmentManager?.beginTransaction()?.let { ft ->
            ft.add(detailFragment, TAG_DETAIL)
            ft.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_layout_grid -> {
                photoFolderAdapter.setItemStyle(ItemStyle.GRID)
                true
            }
            R.id.action_layout_default -> {
                photoFolderAdapter.setItemStyle(ItemStyle.DEFAULT)
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}