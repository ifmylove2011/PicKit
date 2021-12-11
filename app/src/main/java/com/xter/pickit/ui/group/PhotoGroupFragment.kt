package com.xter.pickit.ui.group

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.xter.pickit.R
import com.xter.pickit.databinding.FragmentPhotoAlbumBinding
import com.xter.pickit.databinding.FragmentPhotoGroupBinding
import com.xter.pickit.entity.LocalMediaGroup
import com.xter.pickit.ext.GROUP_KEY
import com.xter.pickit.ext.ViewModelFactory
import com.xter.pickit.kit.L
import com.xter.pickit.ui.album.ItemStyle
import com.xter.pickit.ui.album.PhotoAlbumViewModel
import java.lang.StringBuilder
import java.util.*

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
//        photoGroupVM.choiceModeOpen.observe(viewLifecycleOwner,{
//            photoGroupAdapter.notifyDataSetChanged()
//        })
        photoGroupVM.selectGroupNum.value = 0
        photoGroupVM.selectGroupNum.observe(viewLifecycleOwner, { selectNum ->
            L.i("select=$selectNum")
            (activity as AppCompatActivity).supportActionBar?.let { toolbar ->
                if (selectNum > 0) {
                    if (toolbar.title.toString().contains("(")) {
                        toolbar.title = toolbar.title?.let {
                            val title = it.substring(0, it.indexOf("("))
                            L.i("title=$title")
                            "$title($selectNum)"
                        }
                    } else {
                        toolbar.title = "${toolbar.title}($selectNum)"
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

        photoGroupVM.loadGroups()
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
            R.id.action_delete -> {
                deleteGroupDialog()
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

    fun deleteGroupDialog() {
        photoGroupAdapter.getSelectGroups().let { list ->
            if (list.isNotEmpty()) {
                val sb = StringBuilder()
                list.map { lmg ->
                    sb.append(lmg.name).append("\n")
                }
                sb.deleteCharAt(sb.length - 1)
                AlertDialog.Builder(requireContext())
                    .setTitle("是否删除分组")
                    .setMessage(sb.toString())
                    .setCancelable(false)
                    .setPositiveButton(
                        R.string.sure
                    ) { dialog, _ ->
                        photoGroupVM.deleteGroups(list)
                        dialog?.dismiss()
                    }
                    .setNegativeButton(
                        R.string.cancel
                    ) { dialog, _ -> dialog?.dismiss() }
                    .create()
                    .show()
            } else {
                Snackbar.make(photoGroupBinding.rvGroup, "未选中", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        photoGroupVM.choiceModeOpen.value = false
    }
}