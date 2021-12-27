package com.xter.pickit.ui.widget;

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * @Author XTER
 * @Date 2021/11/29 14:12
 * @Description 方形视图
 */
class SquareImageView : androidx.appcompat.widget.AppCompatImageView {
    constructor(context: Context?) : super(context!!) {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //以宽为高
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}