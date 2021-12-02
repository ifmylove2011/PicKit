package com.xter.pickit.ui.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.xter.pickit.databinding.FragmentPhotoDetailBinding
import com.xter.pickit.kit.L

/**

 */
class PhotoDetailFragment : Fragment() {

    private lateinit var photoVM: PhotoAlbumViewModel
    private lateinit var detailBinding: FragmentPhotoDetailBinding

    private lateinit var photoDetailAdapter: PhotoDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        photoVM = ViewModelProvider(requireActivity()).get(PhotoAlbumViewModel::class.java)
        L.w(photoVM.toString())
        detailBinding = FragmentPhotoDetailBinding.inflate(inflater, container, false).apply {
            this.vm = photoVM
        }
        setHasOptionsMenu(true)
        return detailBinding!!.root
    }


}