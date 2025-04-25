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
    
    external fun terminateProcess()
    external fun getXpConstant(): Int
    external fun getXpStringConstant(): String
    external fun getXpsStringConstant(): String
    private val XposedModulusSize = getXpConstant() // 签名 模数大小
    private val XposedSerialNumber = getXpStringConstant() // 签名 序列号
    private val XposedSinMain = getXpsStringConstant() // 签名 原始数据
    private val XposedPublicExponent = BigInteger.valueOf(65537) // 签名 公钥指数

    override fun onCreate() {
        super.onCreate()
        Log.d("ShizukuSample", "${javaClass.simpleName} onCreate")
        if (!isSui) {
        // 多进程支持，
            ShizukuProvider.requestBinderForNonProviderProcess(this)
        }
        
        if (XposedStartMian() && XposedStartPublicKey() && XposedStartRaw()) {
            
        } else {
          // 可以在这里执行其他操作，比如关闭应用或显示警告
          Toast.makeText(this, "官方签名已被寡改，注意代码执行安全！", Toast.LENGTH_LONG).show()
                SystemStartTask()
        }
    }
    
    private fun XposedStartMian(): Boolean {
        return try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signingInfo = packageInfo.signingInfo
            val signatures = signingInfo?.apkContentsSigners
            val currentSignature = signatures?.get(0) ?: return false

            // 获取 签名 证书并获取 签名 序列号
            val certFactory = CertificateFactory.getInstance("X.509")
            val cert = certFactory.generateCertificate(currentSignature.toByteArray().inputStream()) as X509Certificate
            val serialNumber = cert.serialNumber.toString()

            serialNumber == XposedSerialNumber
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun XposedStartIO() {
        try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signingInfo = packageInfo.signingInfo
            val signatures = signingInfo?.apkContentsSigners
            val currentSignature = signatures?.get(0) ?: return

            // 获取 签名 证书
            val certFactory = CertificateFactory.getInstance("X.509")
            val cert = certFactory.generateCertificate(currentSignature.toByteArray().inputStream()) as X509Certificate

            // 获取 签名 有效期
            val currentNotBefore = cert.notBefore
            val currentNotAfter = cert.notAfter

            // 预期有效期开始和结束日期
            val dateFormat = SimpleDateFormat("yyyy-MM-dd 'GMT'XXX HH:mm:ss", Locale.getDefault())
            
            Toast.makeText(this, "有效期始: ${dateFormat.format(currentNotBefore)}", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "有效期至: ${dateFormat.format(currentNotAfter)}", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun XposedStartPublicKey(): Boolean {
        return try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signingInfo = packageInfo.signingInfo
            val signatures = signingInfo?.apkContentsSigners
            val currentSignature = signatures?.get(0) ?: return false

            val certFactory = CertificateFactory.getInstance("X.509")
            val cert = certFactory.generateCertificate(currentSignature.toByteArray().inputStream()) as X509Certificate
            val publicKey = cert.publicKey

            // 获取 签名 公钥的模数和指数
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec::class.java)

            val modulus = keySpec.modulus
            val publicExponent = keySpec.publicExponent

            // 验证 签名 模数大小和公钥 签名 指数
            modulus.bitLength() == XposedModulusSize && publicExponent == XposedPublicExponent
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun XposedStartRaw(): Boolean {
        return try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signingInfo = packageInfo.signingInfo
            val signatures = signingInfo?.apkContentsSigners ?: return false

            val currentSignature = signatures[0]

            // 获取 签名 原始数据
            val rawSignature = currentSignature.toCharsString()

            rawSignature == XposedSinMain
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun SystemStartTask() {
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            StartApplication()
        }
    }
    
    private fun StartApplication() {
        // 结束并停止进程
        System.exit(1)
        terminateProcess()
        Process.killProcess(Process.myPid())
    }
}