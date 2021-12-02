package com.xter.pickit.ui.album

import android.os.Bundle
import android.view.*
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
        L.w(photoVM.toString())
        photoBinding = FragmentPhotoAlbumBinding.inflate(inflater, container, false).apply {
            this.vm = photoVM
        }
        setHasOptionsMenu(true)
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
                    bundle.putParcelable(KEY_MEDIA_DATA, mediaData)
                    findNavController().navigate(R.id.action_nav_content_to_nav_photo_detail, bundle)
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
            photoVM.loadMediaData(requireContext(), folder)
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