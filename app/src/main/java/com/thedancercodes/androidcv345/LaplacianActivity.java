package com.thedancercodes.androidcv345;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.chip.Chip;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LaplacianActivity extends AppCompatActivity {

    BaseLoaderCallback baseLoaderCallback;

    private ImageView mImageView;
    private TextView mTextView;
    private Chip mChip;

    private static final String TAG = "LaplacianActivity";
    public static final int PICK_IMAGE_REQUEST_CODE = 1001;
    private static final int BLUR_THRESHOLD = 5;
    @NotNull
    public static final String BLURRED_IMAGE = "BLURRED IMAGE";
    @NotNull
    public static final String NOT_BLURRED_IMAGE = "NOT BLURRED IMAGE";
    @NotNull
    public static final String NOT_SURE = "NOT SURE";

    // Specify a unique name for the file
    String currentImagePath = null;

    private static final int IMAGE_REQUEST = 1;

    private String [] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_PHONE_STATE", "android.permission.SYSTEM_ALERT_WINDOW","android.permission.CAMERA"};


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        mImageView = findViewById(R.id.imageView);
        mTextView = findViewById(R.id.textView);
        mChip = findViewById(R.id.chip);
        TextView textCpuArchitecture = findViewById(R.id.textCpuArchitecture);

        textCpuArchitecture.setText(getString(R.string.cpu_architecture, System.getProperty("os.arch")));

        int requestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }

    }

    public void takePicture(View view) {

        // Capture Image & save it into the file path
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check whether there's an activity capable of handling this Intent.
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;

            try {
                imageFile = getImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider", imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                startActivityForResult(cameraIntent, IMAGE_REQUEST);
            }
        }
    }

    public void displayImage(View view) {

        Intent intent = new Intent(this, DisplayImage.class);
        intent.putExtra("image_path", currentImagePath);
        startActivity(intent);

    }

    private File getImageFile() throws IOException {

        // Timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageName = "jpg_" + timeStamp + "_";

        // Use external private storage space
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Create the ImageFile
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);

        currentImagePath = imageFile.getAbsolutePath();

        return imageFile;
    }

    // Receive the result from the camera application
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode==IMAGE_REQUEST && resultCode==RESULT_OK) {

            Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);

            mImageView.setImageBitmap(bitmap);

            // Covert ImagePath to Matrix
            Mat img = Imgcodecs.imread(currentImagePath, Imgcodecs.IMREAD_COLOR);

            // Check if image is loaded fine
            if (img.empty()) {
                Log.d(TAG, "Error opening image");
                Toast.makeText(this, "Error opening image", Toast.LENGTH_SHORT).show();
            } else {
                laplacianFilter(img);
            }
        }
    }

    public void laplacianFilter(Mat img) {
        // Declare the variables we are going to use
        Mat src_gray = new Mat();
        Mat lap = new Mat();
        int kernel_size = 3;
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_64F;

        // Reduce noise by blurring with a Gaussian filter ( kernel size = 3 )
        Imgproc.GaussianBlur(img, img, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);

        // Convert the image to grayscale
        Imgproc.cvtColor(img, src_gray, Imgproc.COLOR_RGBA2GRAY);

        // Leveraging the Laplacian filter
        Imgproc.Laplacian(src_gray, lap, ddepth);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble dev = new MatOfDouble();

        Core.meanStdDev(lap, mean, dev);
        double value = dev.get(0, 0)[0];

        Log.d("laplacianFilter", String.valueOf(value));

        mChip.setVisibility(View.VISIBLE);

        if (value < 3) {
            Toast.makeText(this, BLURRED_IMAGE, Toast.LENGTH_LONG).show();
            mTextView.setText(getString(R.string.result_from_opencv, BLURRED_IMAGE, String.valueOf(BLUR_THRESHOLD), String.valueOf(value)));
            mChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blurred_image)));
            mChip.setText(BLURRED_IMAGE);
            mChip.setChipIcon(getResources().getDrawable(R.drawable.ic_error_white_24dp));

        } else if(value >= 3.5 && value <= 4.5 ) {
            Toast.makeText(this, NOT_SURE, Toast.LENGTH_LONG).show();
            mTextView.setText(getString(R.string.result_from_opencv, NOT_SURE, String.valueOf(BLUR_THRESHOLD), String.valueOf(value)));
            mChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.not_sure)));
            mChip.setText(NOT_SURE);
            mChip.setChipIcon(getResources().getDrawable(R.drawable.ic_visibility_white_24dp));

        } else if (value >= 5){
            Toast.makeText(this, NOT_BLURRED_IMAGE, Toast.LENGTH_LONG).show();
            mTextView.setText(getString(R.string.result_from_opencv, NOT_BLURRED_IMAGE, String.valueOf(BLUR_THRESHOLD), String.valueOf(value)));
            mChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.not_blurred_image)));
            mChip.setText(NOT_BLURRED_IMAGE);
            mChip.setChipIcon(getResources().getDrawable(R.drawable.ic_check_circle_white_24dp));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

        // Load default libopencv_java.so
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There's something wrong", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(getApplicationContext(), "Libraries Loaded!", Toast.LENGTH_SHORT).show();
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }
}
