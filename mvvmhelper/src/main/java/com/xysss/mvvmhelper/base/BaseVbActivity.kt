package com.xysss.mvvmhelper.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.noober.background.BackgroundLibrary
import com.xysss.mvvmhelper.R
import com.xysss.mvvmhelper.ext.*
import com.xysss.mvvmhelper.net.LoadStatusEntity
import com.xysss.mvvmhelper.net.LoadingDialogEntity
import com.xysss.mvvmhelper.net.LoadingType
import com.xysss.mvvmhelper.net.manager.NetState
import com.xysss.mvvmhelper.net.manager.NetworkStateManager
import com.xysss.mvvmhelper.widget.BaseEmptyCallback
import com.xysss.mvvmhelper.widget.BaseErrorCallback
import com.xysss.mvvmhelper.widget.BaseLoadingCallback
import java.lang.reflect.ParameterizedType

/**
 * Author:bysd-2
 * Time:2021/10/911:33
 */
abstract class BaseVbActivity<VM : BaseViewModel,VB: ViewBinding> : AppCompatActivity(), BaseIView {

    val layoutId: Int = 0
    var dataBindView : View? = null
    lateinit var mViewBinding: VB
    //界面状态管理者
    lateinit var uiStatusManger: LoadService<*>
    //当前Activity绑定的 ViewModel
    lateinit var mViewModel: VM
    //toolbar 这个可替换成自己想要的标题栏
    private var mTitleBarView: View? = null
    abstract fun initView(savedInstanceState: Bundle?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewBind()
        setContentView(R.layout.activity_base)
        //生成ViewModel
        mViewModel = createViewModel()
        //初始化 status View
        initStatusView(savedInstanceState)
        //注册界面响应事件
        addLoadingUiChange(mViewModel)
        //初始化绑定observer
        initObserver()
        //初始化请求成功方法
        onRequestSuccess()
        //初始化绑定点击方法
        onBindViewClick()
        //网络监听
        NetworkStateManager.instance.mNetworkStateCallback.observe(this){
            onNetworkStateChanged(it)
        }
    }

    /**
     * 点击事件方法 配合setOnclick()拓展函数调用，做到黄油刀类似的点击事件
     */
    open fun onBindViewClick() {}

    /**
     * 注册 UI 事件 监听请求时的回调UI的操作
     */
    fun addLoadingUiChange(viewModel: BaseViewModel) {
        viewModel.loadingChange.run {
            loading.observe(this@BaseVbActivity) {
                when (it.loadingType) {
                    LoadingType.LOADING_DIALOG -> {
                        if (it.isShow) {
                            showLoading(it)
                        } else {
                            dismissLoading(it)
                        }
                    }
                    LoadingType.LOADING_CUSTOM -> {
                        if (it.isShow) {
                            showCustomLoading(it)
                        } else {
                            dismissCustomLoading(it)
                        }
                    }
                    LoadingType.LOADING_XML -> {
                        if (it.isShow) {
                            showLoadingUi()
                        }
                    }
                }
            }
            showEmpty.observe(this@BaseVbActivity) {
                onRequestEmpty(it)
            }
            showError.observe(this@BaseVbActivity) {
                //如果请求错误 并且loading类型为 xml 那么控制界面显示为错误布局
                if (it.loadingType == LoadingType.LOADING_XML) {
                    showErrorUi(it.errorMessage)
                }
                onRequestError(it)
            }
            showSuccess.observe(this@BaseVbActivity) {
                showSuccessUi()
            }
        }
    }

    /**
     * 创建 ViewBinding
     */
    private fun initViewBind(): View? {
        //利用反射 根据泛型得到 ViewDataBinding
        mViewBinding = inflateBinding()
        BackgroundLibrary.inject(this)
        dataBindView = mViewBinding.root
        return mViewBinding.root
    }

    /**
     * 创建观察者
     */
    open fun initObserver() {}

    private fun initStatusView(savedInstanceState: Bundle?) {
        mTitleBarView = getTitleBarView()
        mTitleBarView?.let {
            findViewById<LinearLayout>(R.id.baseRootView).addView(it, 0)
            //是否隐藏标题栏
            it.visibleOrGone(showToolBar())
        }
        initImmersionBar()
        findViewById<FrameLayout>(R.id.baseContentView).addView(if (dataBindView == null) LayoutInflater.from(this).inflate(layoutId, null) else dataBindView)
        uiStatusManger = LoadSir.getDefault().register(if (getLoadingView() == null) findViewById<FrameLayout>(
            R.id.baseContentView) else getLoadingView()!!) {
            onLoadRetry()
        }

        findViewById<FrameLayout>(R.id.baseContentView).post {
            initView(savedInstanceState)
        }
    }


    /**
     * 是否隐藏 标题栏 默认显示
     */
    open fun showToolBar(): Boolean {
        return true
    }

    /**
     * 初始化沉浸式
     * Init immersion bar.
     */
    protected open fun initImmersionBar() {
        //设置共同沉浸式样式
        mTitleBarView?.let {
            if (showToolBar()) {
                ImmersionBar.with(this).titleBar(it).init()
            }
        }
    }

    /**
     * 网络变化监听 子类重写
     */
    open fun onNetworkStateChanged(netState: NetState) {}

    /**
     * 创建viewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVmClazz(this))
    }

    /**
     * 请求列表数据为空时 回调
     * @param loadStatus LoadStatusEntity
     */
    override fun onRequestEmpty(loadStatus: LoadStatusEntity) {
        showEmptyUi()
    }

    /**
     * 请求接口失败回调，如果界面有请求接口，需要处理错误业务，请实现它 如果不实现那么 默认吐司错误消息
     * @param loadStatus LoadStatusEntity
     */
    override fun onRequestError(loadStatus: LoadStatusEntity) {
        loadStatus.errorMessage.toast()
    }

    /**
     * 请求成功的回调放在这里面 没干啥就是取了个名字，到时候好找
     */
    override fun onRequestSuccess() {}

    /**
     * 空界面，错误界面 点击重试时触发的方法，如果有使用 状态布局的话，一般子类都要实现
     */
    override fun onLoadRetry() {}

    /**
     * 显示 成功状态界面
     */
    override fun showSuccessUi() {
        uiStatusManger.showSuccess()
    }

    /**
     * 显示 错误 状态界面
     * @param errMessage String
     */
    override fun showErrorUi(errMessage: String) {
        uiStatusManger.showCallback(BaseErrorCallback::class.java)
    }

    /**
     * 显示 空数据 状态界面
     */
    override fun showEmptyUi() {
        uiStatusManger.showCallback(BaseEmptyCallback::class.java)
    }

    /**
     * 显示 loading 状态界面
     */
    override fun showLoadingUi() {
        uiStatusManger.showCallback(BaseLoadingCallback::class.java)
    }

    /**
     * 显示自定义loading 在请求时 设置 loadingType类型为LOADING_CUSTOM 时才有效 可以根据setting中的requestCode判断
     * 具体是哪个请求显示该请求自定义的loading
     * @param setting LoadingDialogEntity
     */
    override fun showCustomLoading(setting: LoadingDialogEntity) {
        showLoadingExt(setting.loadingMessage)
    }

    /**
     * 隐藏自定义loading 在请求时 设置 loadingType类型为LOADING_CUSTOM 时才有效 可以根据setting中的requestCode判断
     * 具体是哪个请求隐藏该请求自定义的loading
     * @param setting LoadingDialogEntity
     */
    override fun dismissCustomLoading(setting: LoadingDialogEntity) {
        dismissLoadingExt()
    }

    override fun showLoading(setting: LoadingDialogEntity) {
        showLoadingExt(setting.loadingMessage)
    }

    override fun dismissLoading(setting: LoadingDialogEntity) {
        dismissLoadingExt()
    }

    override fun finish() {
        dismissLoadingExt()
        super.finish()
    }

}