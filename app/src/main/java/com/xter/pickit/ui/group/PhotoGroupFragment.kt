package com.xter.pickit.ui.group

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.xter.pickit.R
import com.xter.pickit.databinding.FragmentPhotoAlbumBinding
import com.xter.pickit.databinding.FragmentPhotoGroupBinding
import com.xter.pickit.ext.GROUP_KEY
import com.xter.pickit.ext.ViewModelFactory
import com.xter.pickit.ui.album.ItemStyle
import com.xter.pickit.ui.album.PhotoAlbumViewModel

/**
 * 以分组为视图
 */
class PhotoGroupFragment : Fragment() {

    private lateinit var photoGroupVM: PhotoGroupViewModel
    private lateinit var photoGroupBinding: FragmentPhotoGroupBinding

    private lateinit var photoGroupAdapter: PhotoGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        photoGroupVM = ViewModelFactory.create(GROUP_KEY, PhotoGroupViewModel::class.java)
        photoGroupBinding = FragmentPhotoGroupBinding.inflate(inflater, container, false).apply {
            this.vm = photoGroupVM
        }
        setHasOptionsMenu(true)
        return photoGroupBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        photoGroupBinding.lifecycleOwner = this.viewLifecycleOwner
        photoGroupBinding.rvGroup.apply {
            layoutManager = GridLayoutManager(this.context, 2)
            photoGroupBinding.vm?.let { VM ->
                photoGroupAdapter = PhotoGroupAdapter(VM)
            }
            photoGroupAdapter.setItemClickListener(object : OnGroupClickListener {
                override fun onItemClick(groupFolder: GroupViewHolder, position: Int) {
                }

                override fun onItemLongClick(groupFolder: GroupViewHolder, position: Int) {
                }

            })
            adapter = photoGroupAdapter
        }
        photoGroupVM.groupLoadCompleted.observe(viewLifecycleOwner, {
            if (it) {
                photoGroupAdapter.submitList(photoGroupVM.groups.value)
                photoGroupAdapter.notifyDataSetChanged()
            }
        })
        if (photoGroupVM.groups.value == null) {
            photoGroupVM.loadGroups()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.group, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_create -> {
                createGroupDialog()
                true
            }
            R.id.action_layout_grid -> {
                photoGroupAdapter.setItemStyle(ItemStyle.GRID)
                true
            }
            R.id.action_layout_default -> {
                photoGroupAdapter.setItemStyle(ItemStyle.DEFAULT)
                true
            }
            else -> false
        }
    }

    fun createGroupDialog() {
        val etName = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.create_group)
            .setView(etName)
            .setCancelable(false)
            .setPositiveButton(
                R.string.sure
            ) { dialog, _ ->
                photoGroupVM.createNewGroup(etName.text.toString())
                dialog?.dismiss()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog, _ -> dialog?.dismiss() }
            .create()
            .show()
    }
}