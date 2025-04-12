package app.compile
/*
   源代码开源是为了让您观摩我的诗山代码，而不是教您如何使用 Shizuku-13.1.5 版本的依赖
*/
import android.os.Bundle
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.os.Environment
import android.os.RemoteException
import android.net.Uri
import android.text.TextUtils

import android.util.Log
import android.app.Dialog
import android.app.ActivityManager
import android.provider.Settings

import android.system.Os
import android.system.OsConstants
import android.view.Display
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.Menu
import android.view.MenuItem
import android.view.LayoutInflater
import android.view.Gravity
import android.view.GestureDetector
import android.view.MotionEvent

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.DialogInterface
import android.content.Intent
import android.content.Context
import android.content.res.Resources
import android.content.pm.PackageManager

import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import app.compile.databinding.ActivityMainBinding
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Lifecycle

import rikka.shizuku.Shizuku

import java.io.File
import java.io.FileWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.IOException
import java.io.BufferedReader
import java.io.FileReader
import java.util.zip.ZipInputStream
import java.util.Locale
import java.util.UUID
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.text.SimpleDateFormat

import com.google.android.material.appbar.MaterialToolbar
import androidx.appcompat.widget.Toolbar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
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
class MainActivity : AppCompatActivity(), DefaultLifecycleObserver {
    companion object {
        private const val PERMISSION_CODE = 10001
        
    }

    private var shizukuServiceState = false
    private lateinit var binding: ActivityMainBinding
    private var iUserService: IUserService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        lifecycle.addObserver(this)
        
        val folderNames = listOf("shizuku", "data")
        createPrivateExternalDirectories(folderNames)
        createInternalStorageDirectories(folderNames)
        initShizuku()
        addEvent()
    }
    
    override fun onPause() {
        super<AppCompatActivity>.onPause() // 指定超类
        // 进入后台时的逻辑
    }

    override fun onResume() {
        super<AppCompatActivity>.onResume() // 指定超类
        // 返回前台时的逻辑
    }

    private fun initShizuku() {
        // 添加权限申请监听
        Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener)

        // Shizuku 服务启动时调用该监听
        Shizuku.addBinderReceivedListenerSticky(onBinderReceivedListener)

        // Shizuku 服务终止时调用该监听
        Shizuku.addBinderDeadListener(onBinderDeadListener)
    }

    override fun onDestroy(owner: LifecycleOwner) {
       super<DefaultLifecycleObserver>.onDestroy(owner)
        // 移除权限申请监听
        Shizuku.removeRequestPermissionResultListener(onRequestPermissionResultListener)
        Shizuku.removeBinderReceivedListener(onBinderReceivedListener)
        Shizuku.removeBinderDeadListener(onBinderDeadListener)
        Shizuku.unbindUserService(userServiceArgs, serviceConnection, true)
    }

    private val onBinderReceivedListener = {
        shizukuServiceState = true
        Toast.makeText(this, "Shizuku服务已启动", Toast.LENGTH_SHORT).show()
    }

    private val onBinderDeadListener = {
        shizukuServiceState = false
        iUserService = null
        Toast.makeText(this, "Shizuku服务被终止", Toast.LENGTH_SHORT).show()
    }

    private fun addEvent() {
        // 判断权限
        binding.judgePermission.setOnClickListener {
            if (!shizukuServiceState) {
                Toast.makeText(this, "Shizuku服务状态异常", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (checkPermission()) {
                Toast.makeText(this, "已拥有权限", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未拥有权限", Toast.LENGTH_SHORT).show()
            }
        }

        // 动态申请权限
        binding.requestPermission.setOnClickListener {
            if (!shizukuServiceState) {
                Toast.makeText(this, "Shizuku服务状态异常", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            requestShizukuPermission()
        }

        // 连接 Shizuku 服务
        binding.connectShizuku.setOnClickListener {
            if (!shizukuServiceState) {
                Toast.makeText(this, "Shizuku服务状态异常", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!checkPermission()) {
                Toast.makeText(this, "没有 Shizuku 权限", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (iUserService != null) {
                Toast.makeText(this, "已连接 Shizuku 服务", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 绑定 Shizuku 服务
            Shizuku.bindUserService(userServiceArgs, serviceConnection)
        }

        /*  非kotlinx协程
        binding.executeCommand.setOnClickListener {
            if (iUserService == null) {
                Toast.makeText(this, "请先连接 Shizuku 服务", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val command = binding.inputCommand.text.toString().trim()

            // 命令不能为空
            if (TextUtils.isEmpty(command)) {
                Toast.makeText(this, "命令不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                // 使用 StringBuilder 来构建结果
                val resultBuilder = StringBuilder()
                
                // 执行命令，返回执行结果
                val result = exec(command)
                
                // 检查结果
                if (result != null) {
                    resultBuilder.append(result)
                } else {
                    resultBuilder.append("返回结果为null")
                }

                // 将执行结果显示
                binding.executeResult.text = resultBuilder.toString()
            } catch (e: Exception) {
                // 直接输出错误信息
                binding.executeResult.text = e.toString()
                e.printStackTrace()
            }
        }
        */
        // Cmd命令执行，Kotlinx协程异步处理
        binding.executeCommand.setOnClickListener {
            if (iUserService == null) {
                Toast.makeText(this, "请先连接 Shizuku 服务", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val command = binding.inputCommand.text.toString().trim()

            // 命令不能为空
            if (TextUtils.isEmpty(command)) {
                Toast.makeText(this, "命令不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 使用协程来执行命令
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 使用 StringBuilder 来构建结果
                    val resultBuilder = StringBuilder()

                    // 异步执行命令，并设置超时
                    val result = withTimeout(8000) { // 设置超时时间为8秒
                        exec(command)
                    }

                    // 检查结果
                    if (result != null) {
                        resultBuilder.append(result)
                    } else {
                        resultBuilder.append("返回结果为null")
                    }

                    // 在主线程更新 UI
                    withContext(Dispatchers.Main) {
                        binding.executeResult.text = resultBuilder.toString()
                    }
                } catch (e: TimeoutCancellationException) {
                    // 在主线程更新 UI
                    withContext(Dispatchers.Main) {
                        binding.executeResult.text = "响应超时"
                    }
                } catch (e: Exception) {
                    // 在主线程更新 UI
                    withContext(Dispatchers.Main) {
                        binding.executeResult.text = e.toString()
                        e.printStackTrace()
                    }
                }
            }
        }
    }
/*
    @Throws(RemoteException::class)
    private fun exec(command: String): String? {
        val pattern = Pattern.compile("\"([^\"]*)\"")
        val matcher = pattern.matcher(command)

        return if (matcher.find()) {
            val list = ArrayList<String>()
            val pattern2 = Pattern.compile("\"([^\"]*)\"|(\\S+)")
            val matcher2 = pattern2.matcher(command)

            while (matcher2.find()) {
                if (matcher2.group(1) != null) {
                    list.add(matcher2.group(1)!!)
                } else {
                    list.add(matcher2.group(2)!!)
                }
            }

            // 执行包含空格的命令
            iUserService?.execArr(list.toTypedArray())
        } else {
            // 执行不包含空格的命令
            iUserService?.execLine(command)
        }
    } */
     @Throws(RemoteException::class)
    private fun exec(command: String): String? {
          val systemBinPath = "/system/bin/"
         val privateBinPath = "/sdcard/Android/data/adb.shell.shizuku/files/shizuku/"
         val vendorBinPath = "/vendor/bin/"
          val sysextBinPath = "/system_ext/bin/"

         // 匹配命令和参数
        val pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)")
       val matcher = pattern.matcher(command)

          val commandList = ArrayList<String>()

         // 提取命令和参数
          while (matcher.find()) {
               val argument: String = matcher.group(1) ?: matcher.group(2)!!
             commandList.add(argument)
          }

         if (commandList.isEmpty()) {
              throw RemoteException("Command is empty")
         }

         // 获取命令名称（第一个元素是命令）
         val commandName = commandList[0]

        // 如果命令没有指定路径，则添加默认路径
          val fullCommandName = if (commandName.startsWith("/")) {
                commandName // 已经是完整路径，直接使用
            } else {
             "$systemBinPath$commandName" // 添加默认路径
          }

            // 替换命令名称为完整路径
          commandList[0] = fullCommandName

         // 执行命令
     var result: String? = iUserService?.execArr(commandList.toTypedArray())

         // 如果执行结果为空，尝试使用自定义路径
          if (result.isNullOrEmpty()) {
              val customCommandName = "$privateBinPath${commandName.removePrefix(systemBinPath)}"
              commandList[0] = customCommandName
              result = iUserService?.execArr(commandList.toTypedArray())
          }

          // 如果仍然没有结果，尝试使用 /vendor/bin/ 路径
          if (result.isNullOrEmpty()) {
               val vendorCommandName = "$vendorBinPath${commandName.removePrefix(systemBinPath)}"
             commandList[0] = vendorCommandName
             result = iUserService?.execArr(commandList.toTypedArray())
          }
    
          // 如果仍然没有结果，尝试使用 /system_ext/bin/ 路径
          if (result.isNullOrEmpty()) {
                val sysextCommandName = "$sysextBinPath${commandName.removePrefix(systemBinPath)}"
              commandList[0] = sysextCommandName
              result = iUserService?.execArr(commandList.toTypedArray())
          }

        return result
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Toast.makeText(this@MainActivity, "Shizuku服务连接成功", Toast.LENGTH_SHORT).show()
            iUserService = IUserService.Stub.asInterface(iBinder)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Toast.makeText(this@MainActivity, "Shizuku服务连接断开", Toast.LENGTH_SHORT).show()
            iUserService = null
        }
    }

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("rish")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    private fun requestShizukuPermission() {
        if (Shizuku.isPreV11()) {
            Toast.makeText(this, "当前 Shizuku 版本不支持动态申请权限", Toast.LENGTH_SHORT).show()
            return
        }

        if (checkPermission()) {
            Toast.makeText(this, "已拥有 Shizuku 权限", Toast.LENGTH_SHORT).show()
            return
        }

        // 动态申请权限
        Shizuku.requestPermission(PERMISSION_CODE)
    }

    private val onRequestPermissionResultListener = object : Shizuku.OnRequestPermissionResultListener {
        override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            Toast.makeText(
                this@MainActivity,
                if (granted) "Shizuku授权成功" else "Shizuku授权失败",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun checkPermission(): Boolean {
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }
    
    private fun createPrivateExternalDirectories(folderNames: List<String>) {
      folderNames.forEach { folderName ->
         val directory = getExternalFilesDir(folderName)
        
        if (directory != null && !directory.exists()) {
            val success = directory.mkdirs()
            if (success) {
                Log.d("Directory", "Private external directory created: ${directory.absolutePath}")
            } else {
                Log.e("Directory", "Failed to create private external directory")
        }
            } else {
                Log.d("Directory", "Private external directory already exists or is null")
            }
        }
    }

     private fun createInternalStorageDirectories(folderNames: List<String>) {
    folderNames.forEach { folderName ->
        val directory = File(filesDir, folderName)
        
        if (!directory.exists()) {
            val success = directory.mkdirs()
            if (success) {
                Log.d("Directory", "Internal storage directory created: ${directory.absolutePath}")
            } else {
                Log.e("Directory", "Failed to create internal storage directory")
            }
        } else {
            Log.d("Directory", "Internal storage directory already exists")
         }
      }
   }
   
    override fun onCreateOptionsMenu(menu: Menu?):   Boolean {
          menuInflater.inflate(R.menu.menu_main, menu)
    
         return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
           return when (item.itemId) {
              R.id.action_main_1 -> {
            Toast.makeText(this, "喵~", Toast.LENGTH_SHORT).show()
               true
           }
              R.id.action_main_2 -> {
                  Toast.makeText(this, "已跑路，望周知……", Toast.LENGTH_SHORT).show()
                true
           }
             else -> super.onOptionsItemSelected(item)
        }
        
    }
}