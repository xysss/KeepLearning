package com.xysss.mvvmhelper.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
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
 * Time:2021/10/911:40
 */
abstract class BaseVbFragment<VM : BaseViewModel, VB : ViewBinding>  : Fragment(), BaseIView {


    //使用了 ViewBinding 就不需要 layoutId了，因为 会从 VB 泛型 找到相关的view
    val layoutId: Int = 0
    private var _binding: VB? = null
    val mViewBinding: VB get() = _binding!!

    var dataBindView : View? = null
    //界面状态管理者
    lateinit var uiStatusManger: LoadService<*>

    //是否第一次加载
    private var isFirst: Boolean = true

    //当前Fragment绑定的泛型类ViewModel
    lateinit var mViewModel: VM

    //父类activity
    lateinit var mActivity: AppCompatActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initViewDataBind(inflater,container)
        isFirst = true
        val rootView = if (dataBindView == null) {
            inflater.inflate(layoutId, container, false)
        } else {
            dataBindView
        }
        return if (getLoadingView() == null) {
            uiStatusManger = LoadSir.getDefault().register(rootView) {
                onLoadRetry()
            }
            container?.removeView(uiStatusManger.loadLayout)
            uiStatusManger.loadLayout
        } else {
            rootView
        }

    }

    fun initViewDataBind(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = inflateBinding(inflater, container, false)
        dataBindView=mViewBinding.root
        return mViewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 网络变化监听 子类重写
     */
    open fun onNetworkStateChanged(netState: NetState) {}


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as AppCompatActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = createViewModel()
        initStatusView(view, savedInstanceState)
        addLoadingUiChange(mViewModel)
        initObserver()
        onRequestSuccess()
        onBindViewClick()
    }


    private fun initStatusView(view: View, savedInstanceState: Bundle?) {
        getLoadingView()?.let {
            //如果传入了自定义包裹view 将该view注册 做 空 错误 loading 布局处理
            uiStatusManger = LoadSir.getDefault().register(it) {
                onLoadRetry()
            }
        }
        //view加载完成后执行
        view.post {
            initView(savedInstanceState)
        }
    }

    /**
     * 已创建View 执行在 initView 之前，
     * @param savedInstanceState Bundle?
     */
    open fun onCreatedView(savedInstanceState: Bundle?){

    }

    /**
     * 创建viewModel
     */
    private fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVmClazz(this))
    }

    /**
     * 初始化view操作  这个方法会有延迟，因为使用了LoadSir，需要等待LoadSir注册完成后才能执行
     */
    abstract fun initView(savedInstanceState: Bundle?)

    /**
     * 懒加载
     */
    open fun lazyLoadData() {}

    /**
     * 创建观察者
     */
    open fun initObserver() {}

    override fun onResume() {
        super.onResume()
        onVisible()
    }

    /**
     * 是否需要懒加载
     */
    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            view?.post {
                lazyLoadData()
                //在Fragment中，只有懒加载过了才能开启网络变化监听
                NetworkStateManager.instance.mNetworkStateCallback.observe(this, {
                    //不是首次订阅时调用方法，防止数据第一次监听错误
                    if (!isFirst) {
                        onNetworkStateChanged(it)
                    }
                })
                isFirst = false
            }
        }
    }

    /**
     * 子类可传入需要被包裹的View，做状态显示-空、错误、加载
     * 如果子类不覆盖该方法 那么会将整个当前Fragment界面都当做View包裹
     */
    override fun getLoadingView(): View? {
        return null
    }

    /**
     * 点击事件方法 配合setOnclick()拓展函数调用，做到黄油刀类似的点击事件
     */
    open fun onBindViewClick() {}

    /**
     * 注册 UI 事件 监听请求时的回调UI的操作
     */
    fun addLoadingUiChange(viewModel:BaseViewModel) {
        viewModel.loadingChange.run {
            loading.observe(this@BaseVbFragment) {
                when(it.loadingType){
                    //通用弹窗Dialog
                    LoadingType.LOADING_DIALOG ->{
                        if (it.isShow) {
                            showLoading(it)
                        } else {
                            dismissLoading(it)
                        }
                    }
                    //不同的请求自定义loading
                    LoadingType.LOADING_CUSTOM ->{
                        if (it.isShow) {
                            showCustomLoading(it)
                        } else {
                            dismissCustomLoading(it)
                        }
                    }
                    //请求时 xml显示 loading
                    LoadingType.LOADING_XML ->{
                        if (it.isShow) {
                            showLoadingUi()
                        }
                    }
                }
            }
            //当分页列表数据第一页返回空数据时 显示空布局
            showEmpty.observe(this@BaseVbFragment) {
                onRequestEmpty(it)
            }
            //当请求失败时
            showError.observe(this@BaseVbFragment) {
                if (it.loadingType == LoadingType.LOADING_XML) {
                    showErrorUi(it.errorMessage)
                }
                onRequestError(it)
            }
            //如果是 LoadingType.LOADING_XML，当请求成功时 会显示正常的成功布局
            showSuccess.observe(this@BaseVbFragment) {
                showSuccessUi()
            }
        }
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
    override fun onRequestSuccess() {

    }

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


}