package com.example.steganography;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EncodeSelectImageActivity extends AppCompatActivity {

    // Initialize variables
    ImageView image;
    Button galleryBtn, cameraBtn;
    String currentPhotoPath;

    static final int REQUEST_ID = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int REQUEST_IMAGE_GALLERY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode_select_image);

        // Set variables values
        image = findViewById(R.id.encodeSelectImageView);
        galleryBtn = findViewById(R.id.encodeGalleryButton);
        cameraBtn = findViewById(R.id.encodeCameraButton);

        checkPermission();

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryIntent();
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraIntent();
            }
        });
    }

    private void galleryIntent() {
        // Get image from gallery
        Intent getImage = new Intent(Intent.ACTION_PICK);
        getImage.setType("image/*");
        startActivityForResult(getImage, REQUEST_IMAGE_GALLERY);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void cameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Save image
        try {
            File file = createImageFile();
            Uri imageUri = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap originalImage = BitmapFactory.decodeFile(currentPhotoPath);
            // Set image to ImageView
            image.setImageBitmap(originalImage);

            Intent intent = new Intent(this, EncodeActivity.class);
            intent.putExtra("image", currentPhotoPath);
            startActivity(intent);

        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {

            // Set image which taken from gallery to ImageView
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                // Set image to ImageView
                image.setImageBitmap(selectedImage);

                Intent intent = new Intent(this, EncodeActivity.class);
                intent.putExtra("imageUri", imageUri);
                startActivity(intent);
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permission list
                String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
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