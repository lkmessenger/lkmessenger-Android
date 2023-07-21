package org.linkmessenger

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Insets
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.Snackbar
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.billing.view.activities.BuyPremiumActivity
import org.linkmessenger.billing.viewmodel.ProductType
import org.linkmessenger.collections.views.activities.CollectionsActivity
import org.linkmessenger.data.local.entity.Profile
import org.linkmessenger.fire.views.FireActivity
import org.linkmessenger.posts.view.activities.SinglePostViewerActivity
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.profile.view.activities.PostsViewerActivity
import org.linkmessenger.profile.view.activities.ProfileActivity
import org.linkmessenger.profile.view.activities.ShareProfileActivity
import org.linkmessenger.profile.view.bottomsheets.SendRequestBottomSheet
import org.linkmessenger.profile.view.bottomsheets.VerifiedAccountBottomSheet
import org.linkmessenger.reports.views.activities.ReportsActivity
import org.linkmessenger.request.models.ErrorData
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.profiles.manage.ManageProfileActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.Type

fun Int.percent(c:Int):Int{
    return ((c.toFloat()/this.toFloat())*100).toInt()
}
fun Context.openPremiumActivity(){
    this.startActivity(BuyPremiumActivity.newIntent(this, ProductType.Subs))
}
fun Context.sendEmail(emailAddress: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        startActivity(intent)
    }catch (_:Exception){

    }
}
fun Context.showOfficialButtonSheep(manager: FragmentManager, user:ProfileData?){
    if(user==null) return
    if(!user.isVerified && user.type==0) return

    VerifiedAccountBottomSheet.create(user, object : VerifiedAccountBottomSheet.BottomSheetListener{
        override fun onSent() {

        }

    }).show(manager, "BOTTOM")
}
fun Context.isImageExistsInMediaPictures(imageName: String): Boolean {
    val projection = arrayOf(
        MediaStore.Images.Media.DISPLAY_NAME
    )

    val selection = "${MediaStore.Images.Media.DISPLAY_NAME} like ?"
    val selectionArgs = arrayOf("$imageName%")

    val queryUri =if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

//    while (cursor.moveToNext()) {
//        val n = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
//        Log.d("iii", n)
//    }

    this.contentResolver.query(
        queryUri,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        return cursor.count > 0
    }

    return false
}
fun Context.isVideoExistsInMediaVideos(videoName: String): Boolean {
    val projection = arrayOf(
        MediaStore.Video.Media.DISPLAY_NAME
    )

    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    val selection = "${MediaStore.Video.Media.DISPLAY_NAME} like ?"
    val selectionArgs = arrayOf("$videoName%")

    this.contentResolver.query(
        collection,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        return cursor.count > 0
    }

    return false
}
fun Context.getWatermarkIcon(): String {
    val assetManager = this.assets
    val assetFileName = "link.png"

    val inputStream = assetManager.open(assetFileName)
    val file = File.createTempFile("link.png", null, this.cacheDir)
    val outputStream = FileOutputStream(file)

    inputStream.copyTo(outputStream)
    inputStream.close()
    outputStream.close()

    // Use the assetPath as needed
    return file.absolutePath
}
fun Context.saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
    val fileName = "image_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    // Save the image to the MediaStore for Android 10 (Q) and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver: ContentResolver = context.contentResolver
        var outputStream: OutputStream? = null
        var uri: Uri? = null

        try {
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            uri = resolver.insert(collection, contentValues)
            if (uri != null) {
                outputStream = resolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            outputStream?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        return uri
    } else {
        // Save the image to the app's file directory for Android versions prior to 10 (Q)
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(directory, fileName)
        var fileUri: Uri? = null

        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Use FileProvider for Android 7.0 (N) and above
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                // Use the file URI for Android versions prior to 7.0 (N)
                Uri.fromFile(file)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return fileUri
    }
}
fun Context.getScreenSize():Size{
    val windowManager: WindowManager = this.getSystemService(
        Context.WINDOW_SERVICE) as WindowManager

    val size: Size = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = windowManager.currentWindowMetrics
        // Gets all excluding insets
        // Gets all excluding insets
        val windowInsets: WindowInsets = metrics.windowInsets
        val insets: Insets = windowInsets.getInsetsIgnoringVisibility(
            WindowInsets.Type.navigationBars()
                    or WindowInsets.Type.displayCutout()
        )

        val insetsWidth: Int = insets.right + insets.left
        val insetsHeight: Int = insets.top + insets.bottom

        // Legacy size that Display#getSize reports

        // Legacy size that Display#getSize reports
        val bounds: Rect = metrics.bounds
        val legacySize = Size(
            bounds.width() - insetsWidth,
            bounds.height() - insetsHeight
        )
        legacySize
    } else {
        val m = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(m)
        Size(m.widthPixels, m.heightPixels)
    }
    return size
}
fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

//fun AppCompatActivity.changeStatusBarColor(){
//    val isDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
//            Configuration.UI_MODE_NIGHT_YES
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        // Use WindowInsetsController for API level 30 and higher
//        val controller = window.insetsController
//        if (isDarkTheme) {
//            // For dark theme, set status bar text color to white
//            controller?.setSystemBarsAppearance(
//                0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
//            )
//        } else {
//            // For light theme, set status bar text color to black
//            controller?.setSystemBarsAppearance(
//                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
//                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
//            )
//        }
//    } else {
//        // Use deprecated systemUiVisibility for lower API levels
//        if (isDarkTheme) {
//            // For dark theme, set status bar text color to white
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        } else {
//            // For light theme, set status bar text color to black
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
//        }
//    }
//}
fun View.showError(error:ErrorData?){
    if(error!=null){
        try {
            Snackbar.make(this, error.message.toString(), Snackbar.LENGTH_LONG)
                .show()
        }catch (ignore:java.lang.Exception){

        }
    }
}
fun View.showError(error:Exception){
    try {
        Snackbar.make(this, error.localizedMessage ?: "Error", Snackbar.LENGTH_LONG)
            .show()
    }catch (_:Exception){

    }
}
fun Context.openEditProfile(){
    val intent = ManageProfileActivity.getIntent(this)
    this.startActivity(intent)
}
fun Context.openFire(){
    val intent = FireActivity.newIntent(this)
    this.startActivity(intent)
}
fun Context.openPostsView(postId:Long, lastPostId:Long?, type: Type, userId:Int?) {
    val postsIntent = PostsViewerActivity.newIntent(this, type, postId, lastPostId, userId)
    startActivity(postsIntent)
}
fun Context.openProfile(profileId:Int?=null, username:String?=null) {
    try {
        val postsIntent = ProfileActivity.newIntent(this, profileId, username, null)
        startActivity(postsIntent)
    }catch (_:Exception){

    }
}
fun Context.openCollections() {
    val collectionsIntent = CollectionsActivity.newIntent(this)
    startActivity(collectionsIntent)
}
fun Context.openShareProfile(username: String) {
    val shareProfileActivity = ShareProfileActivity.newIntent(this, username)
    startActivity(shareProfileActivity)
}
fun Context.openReportView(postId: Long) {
    val collectionsIntent = ReportsActivity.newIntent(this, postId)
    startActivity(collectionsIntent)
}
fun Context.openComment(postId:Long) {
    val postsIntent = SinglePostViewerActivity.newIntent(this, postId).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(postsIntent)
}
fun Context.shareProfile(username:String?){
    if(username!=null){
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://linkm.me/@$username")
        sendIntent.type = "text/plain"
        if (sendIntent.resolveActivity(packageManager) != null) {
            startActivity(
                Intent.createChooser(
                    sendIntent,
                    getString(R.string.InviteActivity_invite_to_signal)
                )
            )
        }
    }
}
fun View.getVisibleHeightPercentage(): Double {

    val itemRect = Rect()
    val isParentViewEmpty = this.getLocalVisibleRect(itemRect)

    // Find the height of the item.
    val visibleHeight = itemRect.height().toDouble()
    val height = this.measuredHeight

    val viewVisibleHeightPercentage = visibleHeight / height * 100

    return if(isParentViewEmpty){
        viewVisibleHeightPercentage
    }else{
        0.0
    }
}
