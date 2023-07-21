package org.linkmessenger.telegrambot.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.ActivityNotificationsBinding
import org.thoughtcrime.securesms.databinding.ActivityTelegramBotBinding

class TelegramBotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTelegramBotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelegramBotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.openBot.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/linkmessengerme_bot"))
            startActivity(intent)
        }


    }
}