package com.xter.pickit.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import com.xter.pickit.R
import com.xter.pickit.ext.STACK_NUM
import com.xter.pickit.kit.L


/**
 * @Author XTER
 * @Date 2021/11/30 11:09
 * @Description 方形视图，容纳网格式层叠式的ImageView
 */
class ImageContainer : ViewGroup {

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

    private var mode = 1
    private var mStackNum = 3
    private var mStackSpan = 0

    private var mImageViews = ArrayList<ImageView>()

    private fun init(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ImageContainer)
        mode = a.getInt(R.styleable.ImageContainer_mode, MODE_GRID)

        mRow = a.getInt(R.styleable.ImageContainer_row, ROW)
        mColumn = a.getInt(R.styleable.ImageContainer_column, COLUMN)
        mStackNum = a.getInt(R.styleable.ImageContainer_stack_num, STACKS)
        mStackSpan = a.getDimensionPixelSize(
            R.styleable.ImageContainer_stack_span,
            resources.getDimensionPixelOffset(R.dimen.item_span)
        )
        mPadding = a.getDimensionPixelSize(
            R.styleable.ImageContainer_padding,
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
//        L.i("createImageviews count = $count,mode=$mode")
        for (i in 0 until count) {
            val childImageView = ImageView(context)
            addView(childImageView)
            mImageViews.add(childImageView)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        L.i("left=$l,top=$l,right=$r,bottom=$b")
        val count = getCount()
        if (mode == MODE_GRID) {
            //确定每个视图的宽高
            val unitWidth = (r - l - (mColumn - 1) * mPadding) / mColumn
            val unitHeight = (b - t - (mRow - 1) * mPadding) / mRow
//            L.i("mColumn=$mColumn,mRow=$mRow,count=$count")
//            L.i("unitWidth=$unitWidth,unitHeight=$unitHeight,count=$count")
            //寻位
            for (i in 0 until count) {
                getChildAt(i)?.apply {
                    //当前子视图所在行列
                    val currentRow = i / mColumn
                    val currentColumn = i % mColumn
                    //计算位置
                    val left = currentColumn * mPadding + currentColumn * unitWidth
                    val top = currentRow * mPadding + currentRow * unitHeight
//            L.i("index=$i")
//            L.i("currentRow=$currentRow,currentColumn=$currentColumn")
//            L.i("left=$left,top=$top")
                    layout(left, top, left + unitWidth, top + unitHeight)
                }
            }
        } else if (mode == MODE_STACK) {
            val unitWidth = (r - l - mStackSpan * (mStackNum - 1))
//            L.i("width=$unitWidth,count=$count")
            for (i in 0 until count) {
                getChildAt(i)?.apply {
                    val left = mStackSpan * i
                    val top = mStackSpan * (count - i - 1)
                    //先出现的视图在上层
                    z = top.toFloat()
                    layout(left, top, left + unitWidth, top + unitWidth)
                }
            }
        }
    }

    fun getCount(): Int {
        return if (mode == MODE_GRID) mRow * mColumn else if (mode == MODE_STACK) mStackNum else 1
    }

    fun setRow(row: Int) {
        if (mRow != row && mode == MODE_GRID) {
            mRow = if (row > 0) {
                row
            } else {
                ROW
            }
        }
    }

    fun setColumn(column: Int) {
        if (mColumn != column && mode == MODE_GRID) {
            mColumn = if (column > 0) {
                column
            } else {
                COLUMN
            }
        }
    }

    fun setPadding(padding: Int) {
        val metrics = resources.displayMetrics
        mPadding = TypedValue.complexToDimensionPixelSize(
            padding,
            metrics
        )
    }

    fun setMode(mode: Int) {
        if (this.mode != mode) {
            this.mode = mode
        }
    }

    fun setStackNum(stackNum: Int) {
        if (this.mStackNum != stackNum && mode == MODE_STACK) {
            this.mStackNum = stackNum
        }
    }

    fun setStackSpan(stackSpan: Int) {
        val metrics = resources.displayMetrics
        mStackSpan = TypedValue.complexToDimensionPixelSize(
            stackSpan,
            metrics
        )
    }

    fun getImageViews(): List<ImageView> {
        return mImageViews
    }

    fun resetViews(newCount: Int) {
        val oldCount = mImageViews.size
        if (oldCount < newCount) {
            for (i in oldCount until newCount) {
                val childImageView = ImageView(context)
                addView(childImageView)
                mImageViews.add(childImageView)
            }
        } else if (oldCount > newCount) {
            removeViews(newCount, oldCount - newCount)
            for (i in oldCount - 1 downTo newCount) {
                mImageViews.removeLast()
            }
        }
    }
}

const val ROW = 1
const val COLUMN = 1
const val MODE_GRID = 1
const val MODE_STACK = 2
const val STACKS = 3