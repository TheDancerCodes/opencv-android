package com.thedancercodes.androidcv345;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LaplacianActivity extends AppCompatActivity {

    BaseLoaderCallback baseLoaderCallback;

    private ImageView mImageView;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private static final String TAG = "LaplacianActivity";
    public static final int PICK_IMAGE_REQUEST_CODE = 1001;
    private static final int BLUR_THRESHOLD = 200;
    @NotNull
    public static final String BLURRED_IMAGE = "BLURRED IMAGE";
    @NotNull
    public static final String NOT_BLURRED_IMAGE = "NOT BLURRED IMAGE";

    // Specify a unique name for the file
    String currentImagePath = null;

    private static final int IMAGE_REQUEST = 1;



//    private void opencvProcess() {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inDither = true;
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        Bitmap image = decodeSampledBitmapFromFile(picFilePath, options, 2000, 2000);
//        int l = CvType.CV_8UC1; //8-bit grey scale image
//        Mat matImage = new Mat();
//        Utils.bitmapToMat(image, matImage);
//        Mat matImageGrey = new Mat();
//        Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);
//
//        Bitmap destImage;
//        destImage = Bitmap.createBitmap(image);
//        Mat dst2 = new Mat();
//        Utils.bitmapToMat(destImage, dst2);
//        Mat laplacianImage = new Mat();
//        dst2.convertTo(laplacianImage, l);
//        Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U);
//        Mat laplacianImage8bit = new Mat();
//        laplacianImage.convertTo(laplacianImage8bit, l);
//
//        Bitmap bmp = Bitmap.createBitmap(laplacianImage8bit.cols(), laplacianImage8bit.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(laplacianImage8bit, bmp);
//        int[] pixels = new int[bmp.getHeight() * bmp.getWidth()];
//        bmp.getPixels(pixels, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight()); // bmp为轮廓图
//
//        int maxLap = -16777216; // 16m
//        for (int pixel : pixels) {
//            if (pixel > maxLap)
//                maxLap = pixel;
//        }
//
//        int soglia = -6118750;
//        if (maxLap <= soglia) {
//            System.out.println("is blur image");
//        }
//        soglia += 6118750;
//        maxLap += 6118750;
//        LogUtil.log("图片位置=" + picFilePath
//                + "\nimage.w=" + image.getWidth() + ", image.h=" + image.getHeight()
//                + "\nmaxLap= " + maxLap + "(清晰范围:0~6118750)"
//                + "\n" + Html.fromHtml("<font color='#eb5151'><b>" + (maxLap <= soglia ? "模糊" : "清晰") + "</b></font>"));
//        opencvEnd = true;
//        isBlur = maxLap <= soglia;
//    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        mImageView = findViewById(R.id.imageView);

    }

    public void takePicture(View view) {
//        Intent imageTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        if (imageTakeIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(imageTakeIntent, REQUEST_IMAGE_CAPTURE);
//        }

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

//     Receive the result from the other application
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode==IMAGE_REQUEST && resultCode==RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");

            Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);

            mImageView.setImageBitmap(bitmap);

            //getImageUri(this, imageBitmap);

            //String imagePath = getImageUri(this, imageBitmap).getPath();
            //Toast.makeText(this, currentImagePath, Toast.LENGTH_SHORT).show();

            // Covert ImagePath to Matrix
            Mat img = Imgcodecs.imread(currentImagePath, Imgcodecs.IMREAD_COLOR);

            // Check if image is loaded fine
            if (img.empty()) {
                Log.d(TAG, "Error opening image");
                Toast.makeText(this, "Error opening image", Toast.LENGTH_SHORT).show();
            } else {
                lap_test(img);
            }

        }

    }

//    public Uri getImageUri(Context inContext, Bitmap inImage) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
//        return Uri.parse(path);
//
//    }

    public void lap_test(Mat img) {
        // Declare the variables we are going to use
        Mat src, src_gray = new Mat(), dst = new Mat();
        int kernel_size = 3;
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;

//        String imageName = Environment.getExternalStorageDirectory() + "/Download/bokeh-336478_1280.jpg";
//        String imageName = Environment.getExternalStorageDirectory() + "/Download/5D8A8192.jpg";

//        src = Imgcodecs.imread(imageName, Imgcodecs.IMREAD_COLOR); // Load an image

        // Check if image is loaded fine
//        if (src.empty()) {
//            Log.d("lap_test", "Error opening image");
//        }

        // Reduce noise by blurring with a Gaussian filter ( kernel size = 3 )
        Imgproc.GaussianBlur(img, img, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);

        // // Convert the image to grayscale
        Imgproc.cvtColor(img, src_gray, Imgproc.COLOR_RGBA2GRAY);

//        Mat abs_dst = new Mat();
//
//        Imgproc.Laplacian( src_gray, dst, ddepth, kernel_size, scale, delta, Core.BORDER_DEFAULT);
//
//        // converting back to CV_8U
//        Core.convertScaleAbs( dst, abs_dst);
//
//        Log.d("lap_test", String.valueOf(abs_dst));

        Mat lap = new Mat();
        Imgproc.Laplacian(src_gray, lap, CvType.CV_64F);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble dev = new MatOfDouble();

        Core.meanStdDev(lap, mean, dev);
        double value = dev.get(0, 0)[0];

        Log.d("lap_test", String.valueOf(value));

        // Old Threshold
        Toast.makeText(this, String.valueOf(value), Toast.LENGTH_LONG).show();

//        DecimalFormat("0.00").format(Math.pow(std.get(0, 0)[0], 2.0)).toDouble()

        // Updated Threshold
        //String value2 = (new DecimalFormat("0.00")).format(Math.pow(dev.get(0, 0)[0], 2.0D));
        int value2 = (int) Math.pow(dev.get(0, 0)[0], 2.0D);

//        Toast.makeText(this, String.valueOf(value2), Toast.LENGTH_LONG).show();
//        Log.d("lap_test", String.valueOf(value2));

        if (value < 5) {
            Toast.makeText(this, BLURRED_IMAGE, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, NOT_BLURRED_IMAGE, Toast.LENGTH_LONG).show();
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
//                        lap_test();
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

    private void lap2() {

        String image = Environment.getExternalStorageDirectory() + "/Download/bokeh-336478_1280.jpg";

        Mat gray = new Mat();
//        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Mat lap = new Mat();
        Imgproc.Laplacian(gray, lap, CvType.CV_64F);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble dev = new MatOfDouble();

        Core.meanStdDev(lap, mean, dev);
        double value = dev.get(0,0)[0];

        Log.d("Lap Val", String.valueOf(value));
    }



    public static Mat LaplacianContrast(Mat img) {
        Mat laplacian = new Mat();
        Imgproc.Laplacian(img, laplacian, img.depth());
        //Imgproc.Laplacian(img, laplacian, img.depth(), 3, 1, 0);
        Core.convertScaleAbs(laplacian, laplacian);
        return laplacian;
    }

}
