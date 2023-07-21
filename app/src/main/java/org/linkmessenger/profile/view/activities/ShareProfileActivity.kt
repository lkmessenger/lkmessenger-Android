package org.linkmessenger.profile.view.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.style.DrawableSource
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.profile.viewmodel.ShareProfileViewModel
import org.linkmessenger.request.models.AddLogParams
import org.linkmessenger.saveBitmapToFile
import org.linkmessenger.showError
import org.linkmessenger.utils.CircleRandomRadius
import org.linkmessenger.utils.PostUtil
import org.signal.glide.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.ActivityShareProfileBinding
import org.thoughtcrime.securesms.util.FileProviderUtil
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class ShareProfileActivity : AppCompatActivity(), KoinComponent {
    private lateinit var binding: ActivityShareProfileBinding
    private val viewModel: ShareProfileViewModel = get()
    private val postRepository:PostRepository = get()

    private val colors = arrayListOf(
        R.drawable.color_share_profile_v1,
        R.drawable.color_share_profile_v2,
        R.drawable.color_share_profile_v3,
        R.drawable.color_share_profile_v4,
        R.drawable.color_share_profile_v5
    )
    private val solids = arrayListOf(
        R.color.share_profile_color1,
        R.color.share_profile_color2,
        R.color.share_profile_color3,
        R.color.share_profile_color4,
        R.color.share_profile_color5,
    )

    companion object{
        fun newIntent(context: Context, username:String): Intent {
            return Intent(context, ShareProfileActivity::class.java).apply {
                putExtra("username", username)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.close.setOnClickListener {
            onBackPressed()
        }

        binding.scan.setOnClickListener {
            scanQrCodeLauncher.launch(null)
        }

        binding.share.setOnClickListener {
            try {
                binding.close.isVisible = false
                binding.scan.isVisible = false
                binding.share.isVisible = false
                val b = getBitmapFromViewUsingCanvas(binding.root)
                binding.close.isVisible = true
                binding.scan.isVisible = true
                binding.share.isVisible = true
                b?.let { it1 ->
                    saveMediaToStorage(it1){ uri ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/*"
                            putExtra(Intent.EXTRA_STREAM, uri)
                        }
                        this.startActivity(Intent.createChooser(shareIntent, null))
                    }
                }
            }catch (e:Exception){
                postRepository.addLog(AddLogParams("share", "error:${e.stackTraceToString()}"))
            }finally {
                binding.close.isVisible = true
                binding.scan.isVisible = true
                binding.share.isVisible = true
            }
        }

        viewModel.username.observe(this){
            try {
                val url = "https://linkm.me/users/$it"

                binding.label.text = url.replace("https://","")

                val inst = createQrVectorOptions {
                    colors {
                        dark = QrVectorColor.Solid(ContextCompat.getColor(this@ShareProfileActivity, solids[viewModel.colorId.value!!]))
                    }

                    shapes {
                        frame = QrVectorFrameShape.RoundCorners(.3f)
                        ball = QrVectorBallShape.Circle(1f)
                        darkPixel = CircleRandomRadius()
                    }

                    logo {
                        drawable = DrawableSource.Resource(R.drawable.ic_link_logo_small)
                        padding = QrVectorLogoPadding.Natural(.05f)
                        shape = QrVectorLogoShape.RoundCorners(.0f)
                        size = .26f
                    }
                }

                val data = QrData.Url(url)
                val drawable : Drawable = QrCodeDrawable(this, data, inst)
                binding.qrCode.setImageDrawable(drawable)
            }catch (e:Exception){
                postRepository.addLog(AddLogParams("qr_generate", "error:${e.stackTraceToString()}"))
            }
        }
        viewModel.colorId.observe(this){
            binding.root.setBackgroundResource(colors[it])
        }

        binding.root.setOnClickListener {
            viewModel.changeColorId()
        }

        val username = intent.getStringExtra("username")
        username?.let {
            viewModel.setUsername(it)
        }
    }
    private val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        when(result){
            is QRResult.QRSuccess -> {
                PostUtil.handleUserUrl(this, result.content.rawValue)
                finish()
            }
            QRResult.QRUserCanceled ->{
//                binding.root.showError(Exception("User canceled"))
            }

            QRResult.QRMissingPermission ->{
                binding.root.showError(Exception("Missing permission"))
            }
            is QRResult.QRError ->{
                binding.root.showError(result.exception)
            }
        }
    }
    private fun getBitmapFromViewUsingCanvas(view: View): Bitmap? {
        // Create a new Bitmap object with the desired width and height
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        // Create a new Canvas object using the Bitmap
        val canvas = Canvas(bitmap)

        // Draw the View into the Canvas
        view.draw(canvas)

        // Return the resulting Bitmap
        return bitmap
    }

    private fun saveMediaToStorage(bitmap: Bitmap, onUriCreated: (Uri) -> Unit) {
        try {
            val uri = this.saveBitmapToFile(this, bitmap)
            uri?.let {
                onUriCreated(uri)
            }
        } catch (e: Exception) {
            Log.d("error", e.toString())
            postRepository.addLog(AddLogParams("saveMediaToStorage", "error:${e.stackTraceToString()}"))
        }
    }
}