package com.xter.pickit.ui.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.xter.pickit.databinding.PhotoAlbumFragmentBinding
import com.xter.pickit.kit.L
import com.xter.pickit.ui.home.HomeViewModel
import pub.devrel.easypermissions.EasyPermissions


class PhotoAlbumFragment : Fragment() {

    companion object {
        fun newInstance() = PhotoAlbumFragment()
    }

    private lateinit var photoVM: PhotoAlbumViewModel
    private lateinit var photoBinding: PhotoAlbumFragmentBinding

    private lateinit var photoAdapter: PhotoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        photoVM = ViewModelProvider(this).get(PhotoAlbumViewModel::class.java)
        photoBinding = PhotoAlbumFragmentBinding.inflate(inflater, container, false).apply {
            this.vm = photoVM
        }
        return photoBinding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        photoBinding.lifecycleOwner = this.viewLifecycleOwner
        photoBinding.rvAblum.apply {
            layoutManager = GridLayoutManager(this.context,3)
            photoBinding.vm?.let { VM ->
                photoAdapter = PhotoAdapter(VM)
            }
            photoAdapter.setItemClickListener(object : OnItemClickListener {
                override fun onItemClick(holderK: ViewHolderK, position: Int) {

                }

                override fun onItemLongClick(holderK: ViewHolderK, position: Int) {

                }

            })
            adapter = photoAdapter
        }
        photoVM.loaded.observe(viewLifecycleOwner,
            {
                L.i("loaded = $it")
                if (it) {
                    //TODO 数据为空
                    photoAdapter.submitList(photoVM.folders.value)
                    photoAdapter.notifyDataSetChanged()
                }
            })
        photoVM.loadMediaSource(this.requireContext())

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}