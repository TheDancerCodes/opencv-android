package com.thedancercodes.androidcv345;

import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class LaplacianActivity extends AppCompatActivity {

    BaseLoaderCallback baseLoaderCallback;


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
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    public void lap_test() {
        // Declare the variables we are going to use
        Mat src, src_gray = new Mat(), dst = new Mat();
        int kernel_size = 3;
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;

//        String imageName = Environment.getExternalStorageDirectory() + "/Download/bokeh-336478_1280.jpg";
        String imageName = Environment.getExternalStorageDirectory() + "/Download/5D8A8192.jpg";

        src = Imgcodecs.imread(imageName, Imgcodecs.IMREAD_COLOR); // Load an image

        // Check if image is loaded fine
        if (src.empty()) {
            Log.d("lap_test", "Error opening image");
        }

        // Reduce noise by blurring with a Gaussian filter ( kernel size = 3 )
//        Imgproc.GaussianBlur(src, src, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);

        // // Convert the image to grayscale
        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_RGBA2GRAY);

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

        Toast.makeText(this, String.valueOf(value), Toast.LENGTH_LONG).show();

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
                        lap_test();
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
            Toast.makeText(getApplicationContext(), "Libraries Loaded!", Toast.LENGTH_SHORT).show();
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
