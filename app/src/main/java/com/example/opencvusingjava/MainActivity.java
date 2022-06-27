package com.example.opencvusingjava;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.WindowManager;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.example.opencvusingjava.utils.Utils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

//implements CameraBridgeViewBase.CvCameraViewListener2
public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private Mat mImageRGB;
    private Mat frame;
    private Mat imgHSV;
    private Mat imgBlur;
    private Mat imgEdge;
    private boolean isCaptured = false;
    private CameraBridgeViewBase cameraBridgeViewBase;
    private final int PERMISSION_CODE = 111;
    private static final int REQUEST_IMAGE_OPEN = 1;
    Bitmap bitmap = null;

    private int H;
    private int W;
    private Net net;

    private ImageView imgClickBtn;
    private ImageView selectGalleryImage;

    // MULTIPLE USER PERMISSION ADDED HERE//
    private final String[] PERMISSION = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Log.d("OPENCV_STATUS", "opencv status : " + OpenCVLoader.initDebug());

        imgClickBtn = findViewById(R.id.imgClickBtn);
        selectGalleryImage = findViewById(R.id.selectGalleryImage);

        if (hasPermission()) {

            cameraBridgeViewBase = findViewById(R.id.camera_view);
            cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
            cameraBridgeViewBase.setCameraPermissionGranted();
            cameraBridgeViewBase.setMaxFrameSize(getScreenWidth(), getScreenHeight());
            cameraBridgeViewBase.setCvCameraViewListener(MainActivity.this);

            imgClickBtn.setOnClickListener(view -> isCaptured = true);
        }

        selectGalleryImage.setOnClickListener(view -> selectImage());

    }


    public void selectImage() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Select Picture"), REQUEST_IMAGE_OPEN);
        //startActivityForResult(i, REQUEST_IMAGE_OPEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_OPEN && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                //bitmap = BitmapFactory.decodeFile(imageUri.getPath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                Intent intent = new Intent(MainActivity.this, ImgPreviewActivity.class);
                intent.putExtra("GalleryImage", byteArray);
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private boolean hasPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSION, PERMISSION_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "all permission granted");
                cameraBridgeViewBase.setCameraPermissionGranted();
            } else {
                Log.d("TAG", "permission denied");
            }
        }
    }

    public BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(MainActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    Log.d("SCREEN_RESOLUTION", "Width : " + getScreenWidth() + "\t" + "Height : " + getScreenHeight());
                    cameraBridgeViewBase.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    public void onCameraViewStarted(int width, int height) {
        //W = getScreenWidth();
        //H = getScreenHeight();

        W = width;
        H = height;
        Log.d("SCREEN_RESOLUTION", "Width : " + W + "\t" + "Height : " + H);
        mImageRGB = new Mat();
        frame = new Mat(W, H, CV_8UC4);
        imgHSV = new Mat(W, H, CV_8UC1);
        imgBlur = new Mat(W, H, CV_8UC1);
        imgEdge = new Mat(W, H, CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        frame.release();
        imgHSV.release();
        imgBlur.release();
        imgEdge.release();
        mImageRGB.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        frame = inputFrame.rgba();
        mImageRGB = inputFrame.rgba();

        Imgproc.cvtColor(frame, imgHSV, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imgHSV, imgBlur, new Size(3, 3), 7);
        Imgproc.Canny(imgBlur, imgBlur, 70, 70 * 3, 3, false);
        Imgproc.adaptiveThreshold(imgBlur, imgEdge, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 5);
        Imgproc.Canny(imgEdge, imgEdge, 10, 10 * 7, 3, false);

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imgEdge, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        int largest_idx = 0;
        double largest_area = 0.0;

        MatOfPoint2f new_mat;
        RotatedRect rotateRect;

        for (int idx = 0; idx < contours.size(); idx++) {
            double area = Imgproc.contourArea(contours.get(idx));  //Find the area of contour
            // && (area > 20000)
            if ((area > largest_area) && (area > 9999)) {
                largest_area = area;
                largest_idx = idx;
            }
            new_mat = new MatOfPoint2f(contours.get(largest_idx).toArray());
            rotateRect = Imgproc.minAreaRect(new_mat);
            drawRotatedRect(frame, rotateRect, new Scalar(255, 255, 0), 2);
            //drawRectangleLine(frame, rotateRect);
        }

        //capture image when press capture button//
        //isCaptured = captureImage(isCaptured, mImageRGB);

        return frame;
    }

    private boolean captureImage(boolean isCaptured, Mat matFrame) {
        if (isCaptured) {
            isCaptured = false;
            Utils.createDirectoryAndSaveFile(matFrame);
            Intent intent = new Intent(MainActivity.this, ImgPreviewActivity.class);
            //intent.putExtra("ImageBitmap", bitmapImg);
            startActivity(intent);
        }
        return isCaptured;
    }

    public static void drawRectangleLine(Mat frame, RotatedRect rotateRect) {
        Point points[] = new Point[4];
        rotateRect.points(points);
        for (int i = 0; i < 4; ++i) {
            Imgproc.line(frame, points[i], points[(i + 1) % 4], new Scalar(0, 255, 0), 1);
        }
    }

    public static void drawRotatedRect(Mat image, RotatedRect rotatedRect, Scalar color, int thickness) {
        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        MatOfPoint points = new MatOfPoint(vertices);
        Imgproc.drawContours(image, Collections.singletonList(points), -1, color, thickness);
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    @Override
    protected void onDestroy() {
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.d("TAG", "onResume");
        if (OpenCVLoader.initDebug()) {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("TAG", "onPause");
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
        super.onPause();
    }
}