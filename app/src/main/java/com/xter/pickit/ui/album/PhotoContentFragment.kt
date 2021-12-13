package com.xter.pickit.ui.album

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.xter.pickit.R
import com.xter.pickit.databinding.FragmentPhotoAlbumBinding
import com.xter.pickit.entity.LocalMediaFolder
import com.xter.pickit.ext.ViewModelFactory
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
//        photoVM = ViewModelProvider(requireActivity()).get(PhotoAlbumViewModel::class.java)
        photoVM = ViewModelFactory.create(PhotoAlbumViewModel::class.java)
        photoBinding = FragmentPhotoAlbumBinding.inflate(inflater, container, false).apply {
            this.vm = photoVM
        }
        setHasOptionsMenu(true)

        return photoBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    Intent(requireActivity(), PhotoDetailActivity::class.java).let { intent ->
                        intent.putExtra(KEY_MEDIA_DATA_POS, position)
                        startActivity(intent)
                    }
                }

                override fun onItemLongClick(contentHolder: ContentViewHolder, position: Int) {
                    //长按选中，并进入多选状态
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
            (activity as AppCompatActivity).supportActionBar?.let { toolbar ->
                toolbar.title = folder.name
            }
            photoVM.loadMediaData(requireContext(), folder)
        }

        photoVM.selectNum.value = 0
        photoVM.selectNum.observe(viewLifecycleOwner, { selectNum ->
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
        photoVM.choiceModeOpenForContent.observe(viewLifecycleOwner, { open ->
            photoContentAdapter.notifyDataSetChanged()
            if (!open) {
                photoVM.selectNum.value = 0
            }
        })
        //监听返回键
        getView()?.apply {
            isFocusableInTouchMode = true
            setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP && photoVM.choiceModeOpenForContent.value == true) {
                        photoVM.choiceModeOpenForContent.value = false
                        return true
                    }
                    return false
                }

            })
        }
    }

    fun showDetailFragment() {
        fragmentManager?.let { fm ->
            fm.beginTransaction()?.let { ft ->
                fm.findFragmentByTag(TAG_DETAIL)?.let { ft.show(it) }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        if (photoVM.pickMode.value == true) {
            inflater.inflate(R.menu.album_skim_pick, menu)
        } else {
            inflater.inflate(R.menu.album_skim, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sure->{
                commitSelectedData()
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            R.id.action_cancel->{
                activity?.supportFragmentManager?.popBackStack()
                true
            }
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

    fun commitSelectedData(){
        val selectedData = photoContentAdapter.getSelectedData()

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}