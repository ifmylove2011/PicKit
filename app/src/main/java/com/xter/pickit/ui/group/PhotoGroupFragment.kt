package com.xter.pickit.ui.group

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.xter.pickit.R
import com.xter.pickit.databinding.FragmentPhotoGroupBinding
import com.xter.pickit.db.SPM
import com.xter.pickit.ext.*
import com.xter.pickit.kit.L
import com.xter.pickit.ui.album.ItemStyle

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
        photoGroupVM = ViewModelFactory.create(KEY_GROUP, PhotoGroupViewModel::class.java)
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
                    val group = groupFolder.binding.group
                    val bundle = Bundle()
                    bundle.putParcelable(KEY_GROUP, group)
                    findNavController().navigate(R.id.action_nav_group_to_nav_group_content, bundle)
                }

                override fun onItemLongClick(groupFolder: GroupViewHolder, position: Int) {
                    //进入多选状态，已被adpater实现了前置功能
                }

            })
            //布局参数
            checkGroupStyle()
            adapter = photoGroupAdapter
        }
        //数据加载完成后，赋予adapter数据
        photoGroupVM.groupLoadCompleted.observe(viewLifecycleOwner, {
            if (it) {
                L.i("groups load")
                photoGroupAdapter.submitList(photoGroupVM.groups.value)
                photoGroupAdapter.notifyDataSetChanged()
            }
        })
        //选择数量，toolbar显示变化
        photoGroupVM.selectGroupNum.value = 0
        photoGroupVM.selectGroupNum.observe(viewLifecycleOwner) { selectNum ->
            (activity as AppCompatActivity).supportActionBar?.let { toolbar ->
                if (selectNum > 0) {
                    if (toolbar.title.toString().contains("(")) {
                        toolbar.title = toolbar.title?.let {
                            val title = it.substring(0, it.indexOf("("))
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
        }
        //监听选择视图的显示与隐藏
        photoGroupVM.choiceModeOpenForGroup.observe(viewLifecycleOwner, { open ->
            photoGroupAdapter.notifyDataSetChanged()
            if (!open) {
                photoGroupVM.selectGroupNum.value = 0
            }
        })
        //监听返回键
        getView()?.apply {
            isFocusableInTouchMode = true
            setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP && photoGroupVM.choiceModeOpenForGroup.value == true) {
                        photoGroupVM.choiceModeOpenForGroup.value = false
                        return true
                    }
                    return false
                }

            })
        }
        //加载数据
        if (photoGroupVM.groups.value == null) {
            photoGroupVM.loadGroups()
        }
        //布局的改变
        photoGroupVM.groupStyle.observe(viewLifecycleOwner, { style ->
            photoGroupVM.loadGroups()
        })
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
                SPM.saveStr(context, CONFIG, KEY_GROUP, ItemStyle.GRID.name)
                photoGroupAdapter.setItemStyle(ItemStyle.GRID)
                true
            }
            R.id.action_layout_default -> {
                SPM.saveStr(context, CONFIG, KEY_GROUP, ItemStyle.DEFAULT.name)
                photoGroupAdapter.setItemStyle(ItemStyle.DEFAULT)
                true
            }
            R.id.action_layout_stack -> {
                SPM.saveStr(context, CONFIG, KEY_GROUP, ItemStyle.STACK.name)
                photoGroupAdapter.setItemStyle(ItemStyle.STACK)
                true
            }
            R.id.action_layout_list -> {
                SPM.saveStr(context, CONFIG, KEY_GROUP, ItemStyle.LIST.name)
                photoGroupAdapter.setItemStyle(ItemStyle.LIST)
                true
            }
            else -> false
        }
    }

    fun checkGroupStyle() {
        val style = SPM.getStr(this.context, CONFIG, KEY_GROUP, ItemStyle.DEFAULT.name)
        if (style == ItemStyle.GRID.name) {
            val row = SPM.getStr(this.context, CONFIG, GRID_ROW, "2").toInt()
            val column = SPM.getStr(this.context, CONFIG, GRID_COLUMN, "2").toInt()
            photoGroupVM.gridSpanPair.value = Pair(row, column)
        } else if (style == ItemStyle.STACK.name) {
            photoGroupVM.stackNum.value = SPM.getStr(this.context, CONFIG, STACK_NUM, "3").toInt()
        }
        photoGroupAdapter.setItemStyle(ItemStyle.valueOf(style))
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
        photoGroupVM.choiceModeOpenForGroup.value = false
    }


}