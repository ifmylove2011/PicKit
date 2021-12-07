package com.xter.pickit.ui.album

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.xter.pickit.R
import com.xter.pickit.databinding.ActivityDetailBinding
import com.xter.pickit.entity.LocalMedia
import com.xter.pickit.ext.ViewModelFactory
import com.xter.pickit.kit.L


class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var detailBinding: ActivityDetailBinding
    private lateinit var photoVM: PhotoAlbumViewModel
    private lateinit var photoDetailAdapter: PhotoDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        detailBinding = ActivityDetailBinding.inflate(layoutInflater)
        photoVM = ViewModelFactory.create(PhotoAlbumViewModel::class.java)
        detailBinding.vm = photoVM
        setContentView(detailBinding.root)

        setSupportActionBar(detailBinding.toolbar)

        initView()
    }

    private fun initView() {
        //设置toolbar的返回键可用
        supportActionBar?.apply {
            setHomeButtonEnabled(true)//设置返回键可用，如果某个页面想隐藏掉返回键比如首页，可以调用mToolbar.setNavigationIcion(null);
            setDisplayShowHomeEnabled(true)
        }
        //初始化vp
        detailBinding.vpImageDetail.apply {
            //初始化adapter
            detailBinding.vm?.let { VM ->
                photoDetailAdapter = PhotoDetailAdapter(VM)
            }
            //加ViewPagers滑动监听
            this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    supportActionBar?.let { toolbar ->
                        toolbar.title = photoVM.images.value?.get(position)?.name
                        toolbar.subtitle = "${position + 1}/${photoVM.images.value?.size}"
                    }
                }
            })
            //item的点击监听
            photoDetailAdapter.setItemClickListener(object : OnImageDetailClickListener {
                override fun onItemClick(detailViewHolder: DetailViewHolder, position: Int) {
                    val mediaData = detailViewHolder.binding.mediaData
                    L.i(mediaData.toString())
                    switchToolView()
                }

                override fun onItemLongClick(detailViewHolder: DetailViewHolder, position: Int) {
                    //TODO 弹出详情
                    popDetailDialog(detailViewHolder.binding.mediaData)
                }

            })
            //setAdapter
            adapter = photoDetailAdapter
        }

        //初始化数据，因VM复用，在这里已经有数据
        photoDetailAdapter.submitList(photoVM.images.value)
        photoDetailAdapter.notifyDataSetChanged()

        //根据索引跳到某一个图片
        intent?.getIntExtra(KEY_MEDIA_DATA_POS, 0)?.let { pos ->
            detailBinding.vpImageDetail.setCurrentItem(pos, false)
            detailBinding.toolbar.subtitle = "${pos + 1}/${photoVM.images.value?.size}"
        }

        detailBinding.toolbar.apply {
            setNavigationOnClickListener { onBackPressed() }
        }
        //进入页面即隐藏上下工具栏
        switchToolView()
    }

    /**
     * 切换toolbar和bottombar的显示和隐藏
     */
    fun switchToolView() {
        detailBinding.appbarlayout.let { apl ->
            if (apl.visibility == VISIBLE) {
                apl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_top_gone))
                apl.visibility = GONE
            } else {
                apl.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_top_visible))
                apl.visibility = VISIBLE
            }
        }
        detailBinding.llBottomMenu.root.let { ll ->
            if (ll.visibility == VISIBLE) {
                ll.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_bottom_gone))
                ll.visibility = GONE
            } else {
                ll.visibility = VISIBLE
                ll.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_bottom_visible))
            }
        }
    }

    fun popDetailDialog(mediaData: LocalMedia?) {
        val view = View.inflate(this, R.layout.dialog_detail_1, null)
        AlertDialog.Builder(this)
                //试一下binding
            .setView(view)
            .setPositiveButton(
                R.string.sure
            ) { dialog, _ -> dialog?.dismiss() }
            .create()
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.image_detail, menu)
        return true
    }
}