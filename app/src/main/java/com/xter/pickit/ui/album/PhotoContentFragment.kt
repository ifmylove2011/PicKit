package com.xter.pickit.ui.album

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.xter.pickit.R
import com.xter.pickit.databinding.FragmentPhotoAlbumBinding
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.kit.L

/**
 * 相册图片视图，以图片为单位展示
 */
class PhotoContentFragment : Fragment() {

    companion object {
        fun newInstance() = PhotoContentFragment()
    }

    private lateinit var photoVM: PhotoAlbumViewModel
    private lateinit var photoBinding: FragmentPhotoAlbumBinding

    private lateinit var photoContentAdapter: PhotoContentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        photoVM = ViewModelProvider(requireActivity()).get(PhotoAlbumViewModel::class.java)
        photoBinding = FragmentPhotoAlbumBinding.inflate(inflater, container, false).apply {
            this.vm = photoVM
        }
        setHasOptionsMenu(true)

        L.d("backStackEntryCount=${this.fragmentManager?.backStackEntryCount}")
        L.d("fragmentSize=${this.fragmentManager?.fragments?.size}")
        return photoBinding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        photoBinding.lifecycleOwner = this.viewLifecycleOwner
        photoBinding.rvAblum.apply {
            //TODO　可动态配置的GRID、LIST
            layoutManager = GridLayoutManager(this.context, 3)
            photoBinding.vm?.let { VM ->
                photoContentAdapter = PhotoContentAdapter(VM)
            }
            photoContentAdapter.setItemClickListener(object : OnImageClickListener {
                override fun onItemClick(contentHolder: ContentViewHolder, position: Int) {
                    val mediaData = contentHolder.binding.mediaData
                    L.i(mediaData.toString())
                    val bundle = Bundle()
                    bundle.putInt(KEY_MEDIA_DATA_POS, position)
                    //使用nav虽然方便，但每次会replace初始化fragment，考虑到detail页面的复用频率，其实应该想办法自行管理
                    findNavController().navigate(R.id.action_nav_content_to_nav_photo_detail,bundle)
//                    photoVM.changePos(position)
                }

                override fun onItemLongClick(contentHolder: ContentViewHolder, position: Int) {

                }

            })
            adapter = photoContentAdapter
        }
        photoVM.contentLoadCompleted.observe(viewLifecycleOwner,
            {
                L.i("loaded = $it")
                if (it) {
                    photoContentAdapter.submitList(photoVM.images.value)
                    photoContentAdapter.notifyDataSetChanged()
                }
            })
        arguments?.getParcelable<LocalMediaFolder>(KEY_FOLDER)?.let { folder ->
            (activity as AppCompatActivity).supportActionBar?.let{ toolbar->
                toolbar.title = folder.name
            }
            photoVM.loadMediaData(requireContext(), folder)
        }
    }

    fun showDetailFragment(){
        fragmentManager?.let{ fm->
            fm.beginTransaction()?.let { ft->
                fm.findFragmentByTag(TAG_DETAIL)?.let { ft.show(it) }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main_browser, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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

    override fun onDestroyView() {
        super.onDestroyView()
    }

}