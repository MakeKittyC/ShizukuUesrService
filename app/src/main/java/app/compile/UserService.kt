package app.compile
/*
   源代码开源是为了让您观摩我的诗山代码，而不是教您如何使用 Shizuku-13.1.5 版本的依赖
*/
import android.os.RemoteException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
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
public class UserService : IUserService.Stub() {

    @Throws(RemoteException::class)
    override fun destroy() {
        System.exit(0)
    }

    @Throws(RemoteException::class)
    override fun exit() {
        destroy()
    }

    @Throws(RemoteException::class)
    override fun execLine(command: String): String {
        return try {
            // 执行 shell 命令
            val process = Runtime.getRuntime().exec(command)
            // 读取执行结果
            readResult(process)
        } catch (e: IOException) {
            throw RemoteException()
        } catch (e: InterruptedException) {
            throw RemoteException()
        }
    }

    @Throws(RemoteException::class)
    override fun execArr(command: Array<String>): String {
        return try {
            // 执行 shell 命令
            val process = Runtime.getRuntime().exec(command)
            // 读取执行结果
            readResult(process)
        } catch (e: IOException) {
            throw RemoteException()
        } catch (e: InterruptedException) {
            throw RemoteException()
        }
    }

    /**
     * 读取执行结果，如果有异常会向上抛
     */
    @Throws(IOException::class, InterruptedException::class)
    private fun readResult(process: Process): String {
        val stringBuilder = StringBuilder()
        // 读取执行结果
        val inputStreamReader = InputStreamReader(process.inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line).append("\n")
        }
        inputStreamReader.close()
        process.waitFor()
        return stringBuilder.toString()
    }
}
