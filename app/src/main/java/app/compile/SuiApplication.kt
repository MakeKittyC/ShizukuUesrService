package app.compile
/*
   源代码开源是为了让您观摩我的诗山代码，而不是教您如何使用 Shizuku-13.1.5 版本的依赖
*/
import android.app.Application
import android.util.Log
import rikka.sui.Sui
/* 
   如果您需要使用自定义R类或其他库中的R类，还请添加import
   示例：import app.compile.R
   如果您需要使用多个库中的R类，还请在代码中使用完整路径
   示例：app.compile.R.id.android
   项目使用的是viewbinding，因此您可以直接使用binding
   示例：binding.android.setOnClickListener
   示例：binding.android.text
   或者完整路径binding
   示例：app.compile.binding.android.setOnClickListener
   示例：app.compile.binding.android.text
*/
class SuiApplication : Application() {

    companion object {
        private var isSui: Boolean = false

        fun isSui(): Boolean {
            return isSui
        }

        init {
            isSui = Sui.init(BuildConfig.APPLICATION_ID)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ShizukuSample", "${javaClass.simpleName} onCreate")
    }
}