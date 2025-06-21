package app.compile
/*
   源代码开源是为了让您观摩我的诗山代码，而不是教您如何使用 Shizuku-13.1.5 版本的依赖
*/
import android.app.Application
import android.os.Process
import android.util.Log
import android.widget.Toast
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Build

import rikka.sui.Sui
import rikka.shizuku.ShizukuProvider

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature

import java.math.BigInteger
import java.security.KeyFactory
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.RSAPublicKeySpec
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.io.IOException
import java.io.BufferedReader

import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

import androidx.annotation.Keep
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
@Keep
class SuiApplication : Application() {

    companion object {
        private var isSui: Boolean = false
                
        fun isSui(): Boolean {
            return isSui
        }
        
        init {
            isSui = Sui.init(BuildConfig.APPLICATION_ID)
           if (!isSui) {
           // 多进程支持，android:process，如果您将ShizukuProvider的android:process与Activity的android:process设置为一致，那么应该可以不用添加多进程支持，如果不一致，那么需要设置，这只是个示例
             ShizukuProvider.enableMultiProcessSupport(true)
           }
           System.loadLibrary("native-lib")
           
        }
        
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ShizukuSample", "${javaClass.simpleName} onCreate")
        if (!isSui) {
        // 多进程支持，
            ShizukuProvider.requestBinderForNonProviderProcess(this)
        }
        SystemStartTask()
    }
    
    private fun SystemStartTask() {
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
        }
    }
}