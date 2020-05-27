package com.thedancercodes.androidcv345

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.chip.Chip
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LaplacianActivity : AppCompatActivity() {
    var baseLoaderCallback: BaseLoaderCallback? = null
    private var mImageView: ImageView? = null
    private var mTextView: TextView? = null
    private var mChip: Chip? = null

    // Image Path variable
    var currentImagePath: String? = null

    private val permissions = arrayOf("android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_PHONE_STATE", "android.permission.SYSTEM_ALERT_WINDOW", "android.permission.CAMERA")

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_layout)
        mImageView = findViewById(R.id.imageView)
        mTextView = findViewById(R.id.textView)
        mChip = findViewById(R.id.chip)

        val textCpuArchitecture = findViewById<TextView>(R.id.textCpuArchitecture)
        textCpuArchitecture.text = getString(R.string.cpu_architecture, System.getProperty("os.arch"))

        val requestCode = 200

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
    }

    fun takePicture(view: View?) { // Capture Image & save it into the file path
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Check whether there's an activity capable of handling this Intent.
        if (cameraIntent.resolveActivity(packageManager) != null) {
            var imageFile: File? = null
            try {
                imageFile = getImageFile
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (imageFile != null) {
                val imageUri = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider", imageFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(cameraIntent, IMAGE_REQUEST)
            }
        }
    }

    fun displayImage(view: View?) {
        val intent = Intent(this, DisplayImage::class.java)
        intent.putExtra("image_path", currentImagePath)
        startActivity(intent)
    }

    // Timestamp
    @get:Throws(IOException::class)
    private val getImageFile: File
        private get() {
            // Timestamp
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(Date())
            val imageName = "jpg_" + timeStamp + "_"

            // Use external private storage space
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            // Create the ImageFile
            val imageFile = File.createTempFile(imageName, ".jpg", storageDir)
            currentImagePath = imageFile.absolutePath
            return imageFile
        }

    // Receive the result from the camera application
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(currentImagePath)
            mImageView!!.setImageBitmap(bitmap)

            // Covert ImagePath to Matrix
            val img = Imgcodecs.imread(currentImagePath, Imgcodecs.IMREAD_COLOR)

            // Check if image is loaded fine
            if (img.empty()) {
                Log.d(TAG, "Error opening image")
                Toast.makeText(this, "Error opening image", Toast.LENGTH_SHORT).show()
            } else {
                laplacianFilter(img)
            }
        }
    }

    fun laplacianFilter(img: Mat?) { // Declare the variables we are going to use
        val src_gray = Mat()
        val lap = Mat()
        val kernel_size = 3
        val scale = 1
        val delta = 0
        val ddepth = CvType.CV_64F

        // Reduce noise by blurring with a Gaussian filter ( kernel size = 3 )
        Imgproc.GaussianBlur(img, img, Size(3.0, 3.0), 0.0, 0.0, Core.BORDER_DEFAULT)

        // Convert the image to grayscale
        Imgproc.cvtColor(img, src_gray, Imgproc.COLOR_RGBA2GRAY)

        // Leveraging the Laplacian filter
        Imgproc.Laplacian(src_gray, lap, ddepth)

        // Calculate meanStdDev to determine threshold value.
        val mean = MatOfDouble()
        val dev = MatOfDouble()
        Core.meanStdDev(lap, mean, dev)

        val value = dev[0, 0][0]

        Log.d("laplacianFilter", value.toString())

        mChip!!.visibility = View.VISIBLE

        // Threshold Conditional
        when {
            value < 3 -> {
                Toast.makeText(this, BLURRED_IMAGE, Toast.LENGTH_LONG).show()
                mTextView!!.text = getString(R.string.result_from_opencv, BLURRED_IMAGE, BLUR_THRESHOLD.toString(), value.toString())
                mChip!!.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blurred_image))
                mChip!!.text = BLURRED_IMAGE
                mChip!!.chipIcon = resources.getDrawable(R.drawable.ic_error_white_24dp)
            }
            value in 3.5..4.5 -> {
                Toast.makeText(this, NOT_SURE, Toast.LENGTH_LONG).show()
                mTextView!!.text = getString(R.string.result_from_opencv, NOT_SURE, BLUR_THRESHOLD.toString(), value.toString())
                mChip!!.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.not_sure))
                mChip!!.text = NOT_SURE
                mChip!!.chipIcon = resources.getDrawable(R.drawable.ic_visibility_white_24dp)
            }
            value >= 5 -> {
                Toast.makeText(this, NOT_BLURRED_IMAGE, Toast.LENGTH_LONG).show()
                mTextView!!.text = getString(R.string.result_from_opencv, NOT_BLURRED_IMAGE, BLUR_THRESHOLD.toString(), value.toString())
                mChip!!.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.not_blurred_image))
                mChip!!.text = NOT_BLURRED_IMAGE
                mChip!!.chipIcon = resources.getDrawable(R.drawable.ic_check_circle_white_24dp)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        baseLoaderCallback = object : BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                super.onManagerConnected(status)
                when (status) {
                    SUCCESS -> {}
                    else -> super.onManagerConnected(status)
                }
            }
        }
        // Load default libopencv_java.so
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(applicationContext, "There's something wrong", Toast.LENGTH_SHORT).show()
        } else {
            // Toast.makeText(getApplicationContext(), "Libraries Loaded!", Toast.LENGTH_SHORT).show();
            (baseLoaderCallback as BaseLoaderCallback).onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    companion object {
        private const val TAG = "LaplacianActivity"
        const val PICK_IMAGE_REQUEST_CODE = 1001
        private const val BLUR_THRESHOLD = 5
        const val BLURRED_IMAGE = "BLURRED IMAGE"
        const val NOT_BLURRED_IMAGE = "NOT BLURRED IMAGE"
        const val NOT_SURE = "NOT SURE"
        private const val IMAGE_REQUEST = 1
    }
}