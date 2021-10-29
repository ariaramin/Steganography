package com.example.steganography;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class DecodeSelectImageActivity extends AppCompatActivity {

    // Initialize variables
    ImageView image;
    Button galleryBtn;
    Bitmap capturedImage;

    static final int REQUEST_ID = 1;
    static final int REQUEST_IMAGE_GALLERY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_select_image);

        // Set variables values
        image = findViewById(R.id.decodeSelectImageView);
        galleryBtn = findViewById(R.id.decodeGalleryButton);

        checkPermission();

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryIntent();
            }
        });
    }

    private void galleryIntent() {
        // Get image from gallery
        Intent getImage = new Intent(Intent.ACTION_PICK);
        getImage.setType("image/*");
        startActivityForResult(getImage, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {

            // Set image which taken from gallery to ImageView
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                // Set image to ImageView
                image.setImageBitmap(selectedImage);

                Intent intent = new Intent(this, DecodeActivity.class);
                intent.putExtra("imageUri", imageUri);
                startActivity(intent);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permission list
                String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, REQUEST_ID);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID) {
            // If permission access
            boolean result = true;
            for (int gResult :
                    grantResults) {
                if (gResult != PackageManager.PERMISSION_GRANTED) {
                    result = false;
                }
            }
            if (!result) {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}