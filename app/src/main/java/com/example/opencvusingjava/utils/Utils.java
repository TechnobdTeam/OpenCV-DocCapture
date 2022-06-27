package com.example.opencvusingjava.utils;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_8UC4;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Utils {

    public static void imageCrop(Bitmap bitmap) {
        Mat imageMat = new Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap, imageMat);
        Mat imgSource = imageMat.clone();

        Mat imageHSV = new Mat(imgSource.size(), CV_8UC4);
        Mat imageBlurr = new Mat(imgSource.size(), CV_8UC4);
        Mat imageA = new Mat(imgSource.size(), CV_32F);
        Imgproc.cvtColor(imgSource, imageHSV, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(5, 5), 0);
        Imgproc.adaptiveThreshold(imageBlurr, imageA, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 5);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imageA, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        Vector<Mat> rectangles = new Vector<Mat>();

        for (int i = 0; i < contours.size(); i++) {
            if (Imgproc.contourArea(contours.get(i)) > 50) {
                Rect rect = Imgproc.boundingRect(contours.get(i));
                if ((rect.height > 30 && rect.height < 120) && (rect.width > 120 && rect.width < 500)) {
                    Rect rec = new Rect(rect.x, rect.y, rect.width, rect.height);
                    rectangles.add(new Mat(imgSource, rec));
                    Imgproc.rectangle(imgSource, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));

                }
            }
        }

        Bitmap analyzed = Bitmap.createBitmap(imgSource.cols(), imgSource.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(imgSource, analyzed);
    }

    public static void createDirectoryAndSaveFile(Bitmap imageToSave) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        String fileName = "img_" + currentTime;

        File direct = new File(Environment.getExternalStorageDirectory(), "DocumentScanner_2022");

        //Log.d("FILE", "Path : "+direct);

        if (!direct.exists()) {
            direct.mkdirs();
        }

        File file = new File(direct , fileName);
        Log.d("FILE", "Path : "+file);
//        if (file.exists()) {
//            file.delete();
//        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createDirectoryAndSaveFile(Mat imageToSave) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        String fileName = "img_" + currentTime + ".jpg";

        File direct = new File(Environment.getExternalStorageDirectory(), "DocumentScanner_2022");

        //Log.d("FILE", "Path : "+direct);
        if (!direct.exists()) {
            direct.mkdirs();
            Log.d("FOLDER", "Path : "+direct);
        }

        String file = direct + "/" + fileName;
        Imgcodecs.imwrite(file, imageToSave);
        Log.d("FILE", "Path : "+file);
    }

    public static boolean deleteFile(String filePath){
        if(filePath != null) {
            File file = new File(filePath);

            if (file.exists()) {
                if (file.delete()) {
                    return true;
                } else {
                    return file.getAbsoluteFile().delete();
                }
            }
        }
        return false;
    }
}
