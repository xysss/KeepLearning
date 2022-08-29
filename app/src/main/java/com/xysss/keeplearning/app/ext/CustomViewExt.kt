import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.xysss.keeplearning.app.ext.colorHashMap
import com.xysss.keeplearning.app.util.SettingUtil
import com.xysss.mvvmhelper.base.appContext

fun RecyclerView.initFloatBtn(floatbtn: FloatingActionButton) {
    //监听recyclerview滑动到顶部的时候，需要把向上返回顶部的按钮隐藏
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        @SuppressLint("RestrictedApi")
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!canScrollVertically(-1)) {
                floatbtn.visibility = View.INVISIBLE
            }
        }
    })
    floatbtn.backgroundTintList = SettingUtil.getOneColorStateList(appContext)
    floatbtn.setOnClickListener {
        val layoutManager = layoutManager as LinearLayoutManager
        //如果当前recyclerview 最后一个视图位置的索引大于等于40，则迅速返回顶部，否则带有滚动动画效果返回到顶部
        if (layoutManager.findLastVisibleItemPosition() >= 40) {
            scrollToPosition(0)//没有动画迅速返回到顶部(马上)
        } else {
            smoothScrollToPosition(0)//有滚动动画返回到顶部(有点慢)
        }
    }
}

fun getRouteWidth(): Float {
    return 20f
}

fun initColorMap() {
    if (colorHashMap.isEmpty()) {
        for (i in 0..255) {
            if (i < 32) {
                colorHashMap[i] = Color.rgb(0, 0, 128 + i * 4)
            }
            if (i == 32) {
                colorHashMap[i] = Color.rgb(0, 0, 255)
            }
            if (i in 33..95) {
                colorHashMap[i] = Color.rgb(0, (i - 32) * 4, 255)
            }
            if (i in 96..159) {
                colorHashMap[i] = Color.rgb(2 + (4 * (i - 96)), 255, 254 - 4 * (i - 96))
            }
            if (i == 160) {
                colorHashMap[i] = Color.rgb(255, 252, 0)
            }
            if (i in 161..223) {
                colorHashMap[i] = Color.rgb(255, 248 - 4 * (i - 161), 0)
            }
            if (i == 224) {
                colorHashMap[i] = Color.rgb(252, 0, 0)
            }
            if (i in 225..255) {
                colorHashMap[i] = Color.rgb(248 - 4 * (i - 225), 0, 0)
            }
        }
    }
}
