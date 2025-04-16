package app.compile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import app.compile.databinding.ActivityHelpBinding
import androidx.cardview.widget.CardView

class HelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpBinding
    
    private val View1 = "还请插入小残页的小穴穴.jpg"
    private val View2 = "ShizukuUesrService现已开源"
    private val View3 = "GitHub：https://github.com/MakeKittyC/ShizukuUesrService"
    private val View4 = "您可以自行构建apk来使用，也可以从GitHub下载apk来使用，或者其他来源也可以，但前提是您所获取的apk变体来源均为安全，否则不要轻易相信其他来源不明的apk变体"
    private val View5 = "Telegram频道：https://t.me/LookCuteKitty"
    private val View6 = "QQ闲聊群：1032883640"
    private val View7 = "开发者：小猫猫，性格温顺小奶狗一枚"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        getViewBindingHelp()
    }
    
    fun getViewBindingHelp(){
        binding.textView1.text = View1
        binding.textView2.text = View2
        binding.textView3.text = View3
        binding.textView4.text = View4
        binding.textView5.text = View5
        binding.textView6.text = View6
        binding.textView7.text = View7
    }
}