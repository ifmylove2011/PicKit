package com.xter.pickit.ui.album

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.xter.pickit.R
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
        setHasOptionsMenu(true)
        return photoBinding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        photoBinding.lifecycleOwner = this.viewLifecycleOwner
        photoBinding.rvAblum.apply {
            layoutManager = GridLayoutManager(this.context, 2)
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
                    photoAdapter.submitList(photoVM.folders.value)
                    photoAdapter.notifyDataSetChanged()
                }
            })
        photoVM.loadMediaSource(this.requireContext())

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_layout_grid -> {
                photoAdapter.setItemStyle(ItemStyle.GRID)
                true
            }
            R.id.action_layout_default -> {
                photoAdapter.setItemStyle(ItemStyle.DEFAULT)
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}