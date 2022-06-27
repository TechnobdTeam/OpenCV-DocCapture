package com.example.opencvusingjava;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.opencvusingjava.utils.Utils;

import java.io.File;

public class ImgPreviewActivity extends AppCompatActivity {

    private ImageView imagePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_preview);

        imagePreview = findViewById(R.id.imagePreview);

        byte[] byteArray = getIntent().getByteArrayExtra("GalleryImage");
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        //Bitmap bitmap = getIntent().getParcelableExtra("GalleryImage");
        imagePreview.setImageBitmap(imageBitmap);
    }
}