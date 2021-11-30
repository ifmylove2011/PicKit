package com.xter.pickit.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import com.xter.pickit.R
import com.xter.pickit.kit.L


/**
 * @Author XTER
 * @Date 2021/11/30 11:09
 * @Description 方形视图，容纳网格式的ImageView
 */
class GridImageContainer : ViewGroup {

    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private var mRow = 0
    private var mColumn = 0

    //视为视图之间的间隔，不重复计算，不分上下左右
    private var mPadding = 0

    private var mImageViews = ArrayList<ImageView>()

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.GridImageContainer)
        mRow = a.getInt(R.styleable.GridImageContainer_row, ROW)
        mColumn = a.getInt(R.styleable.GridImageContainer_column, COLUMN)
        mPadding = a.getDimensionPixelSize(
            R.styleable.GridImageContainer_padding,
            resources.getDimensionPixelOffset(R.dimen.item_padding)
        )
        a.recycle()

        createImageViews()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //以宽为高
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    private fun createImageViews() {
        val count = getCount()
        for (i in 0 until count) {
            val childImageView = ImageView(context)
            addView(childImageView)
            mImageViews.add(childImageView)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        L.i("left=$l,top=$l,right=$r,bottom=$b")
        val count = getCount()
        //确定每个视图的宽高
        val unitWidth = (r - l - (mColumn - 1) * mPadding) / mColumn
        val unitHeight = (b - t - (mRow - 1) * mPadding) / mRow
//        L.i("unitWidth=$unitWidth,unitHeight=$unitHeight")
        //寻位
        for (i in 0 until count) {
            val child = getChildAt(i)
            //当前子视图所在行列
            val currentRow = i / mColumn
            val currentColumn = i % mRow
            //计算位置
            val left = currentColumn * mPadding + currentColumn * unitWidth
            val top = currentRow * mPadding + currentRow * unitHeight
//            L.i("index=$i")
//            L.i("currentRow=$currentRow,currentColumn=$currentColumn")
//            L.i("left=$left,top=$top")
            child.layout(left, top, left + unitWidth, top + unitHeight)
        }
    }

    fun getCount(): Int {
        return mRow * mColumn
    }

    fun setRow(row: Int) {
        mRow = if (row > 0) {
            row
        } else {
            ROW
        }
        postInvalidate()
    }

    fun setColumn(column: Int) {
        mColumn = if (column > 0) {
            column
        } else {
            COLUMN
        }
        postInvalidate()
    }

    fun setPadding(padding: Int) {
        val metrics = resources.displayMetrics
        mPadding = TypedValue.complexToDimensionPixelSize(
            padding,
            metrics
        )
        postInvalidate()
    }

    fun getImageViews(): List<ImageView> {
        return mImageViews
    }
}

const val ROW = 1
const val COLUMN = 1