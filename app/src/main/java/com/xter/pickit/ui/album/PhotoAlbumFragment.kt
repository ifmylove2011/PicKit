package com.xter.pickit.ui.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.xter.pickit.databinding.PhotoAlbumFragmentBinding
import pub.devrel.easypermissions.EasyPermissions




class PhotoAlbumFragment : Fragment() {

    companion object {
        fun newInstance() = PhotoAlbumFragment()
    }

    private lateinit var photoVM: PhotoAlbumViewModel
    private var photoBinding: PhotoAlbumFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        photoBinding = PhotoAlbumFragmentBinding.inflate(inflater, container, false)

        return photoBinding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        photoVM = ViewModelProvider(this).get(PhotoAlbumViewModel::class.java)
        photoVM.queryImage(this.requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        photoBinding = null
    }

}