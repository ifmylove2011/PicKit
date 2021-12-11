package com.xter.pickit.ui.album

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.xter.pickit.R
import com.xter.pickit.databinding.FragmentPhotoDetailBinding
import com.xter.pickit.ext.ViewModelFactory
import com.xter.pickit.kit.L

/**

 */
class PhotoDetailFragment : Fragment() {

//    companion object {
//        @JvmStatic
//        fun newInstance(pos: Int) =
//            PhotoDetailFragment().apply {
//                arguments = Bundle().apply {
//                    putInt(KEY_MEDIA_DATA_POS, pos)
//                }
//            }
//    }

    private lateinit var photoVM: PhotoAlbumViewModel
    private lateinit var detailBinding: FragmentPhotoDetailBinding

    private lateinit var photoDetailAdapter: PhotoDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        photoVM = ViewModelProvider(requireActivity()).get(PhotoAlbumViewModel::class.java)
        photoVM = ViewModelFactory.create(PhotoAlbumViewModel::class.java)
        detailBinding = FragmentPhotoDetailBinding.inflate(inflater, container, false).apply {
            this.vm = photoVM
        }
        setHasOptionsMenu(true)
        return detailBinding!!.root
    }

//    fun changeToolbar(){
//        (activity as AppCompatActivity).supportActionBar?.hide()
//        detailBinding.toolbar.inflateMenu(R.menu.image_detail)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailBinding.lifecycleOwner = this.viewLifecycleOwner
        detailBinding.vpImageDetail.apply {
            detailBinding.vm?.let { VM ->
                photoDetailAdapter = PhotoDetailAdapter(VM)
            }
            this.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    (activity as AppCompatActivity).supportActionBar?.let{ toolbar->
                        toolbar.title = photoVM.images.value?.get(position)?.name
                    }
                }
            })
            photoDetailAdapter.setItemClickListener(object : OnImageDetailClickListener {
                override fun onItemClick(detailViewHolder: DetailViewHolder, position: Int) {
                    val mediaData = detailViewHolder.binding.mediaData
                    L.i(mediaData.toString())
                    (activity as AppCompatActivity).supportActionBar?.let { toolbar ->
                        L.d("showing=${toolbar.isShowing}")
                        if (toolbar.isShowing) {
                            toolbar.hide()
                        } else {
                            toolbar.show()
                        }
                    }
                }

                override fun onItemLongClick(detailViewHolder: DetailViewHolder, position: Int) {

                }

            })
            adapter = photoDetailAdapter
        }
        photoDetailAdapter.submitList(photoVM.images.value)
        photoDetailAdapter.notifyDataSetChanged()

        photoVM.contentLoadCompleted.observe(viewLifecycleOwner,
            {
                L.i("loaded = $it")
                if (it) {
                    photoDetailAdapter.submitList(photoVM.images.value)
                    photoDetailAdapter.notifyDataSetChanged()
                }
            })
//        photoVM.currentPos.observe(viewLifecycleOwner, { pos ->
//            L.i("位置变化${pos}")
//            detailBinding.vpImageDetail.setCurrentItem(pos, false)
//        })
        arguments?.getInt(KEY_MEDIA_DATA_POS)?.let { pos ->
            detailBinding.vpImageDetail.setCurrentItem(pos, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.image_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_copy -> {
                true
            }
            R.id.action_detail-> {
                true
            }
            else -> false
        }
    }
}