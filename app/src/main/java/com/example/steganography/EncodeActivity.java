package com.example.steganography;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EncodeActivity extends AppCompatActivity {

    // Initialize variables
    ImageView image;
    Button saveBtn, galleryBtn, cameraBtn;
    EditText input;
    Bitmap capturedImage;
    String currentPhotoPath;

    static final int REQUEST_ID = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int REQUEST_IMAGE_Gallery = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        image = findViewById(R.id.imageView);
        saveBtn = findViewById(R.id.saveButton);
        galleryBtn = findViewById(R.id.galleryButton);
        cameraBtn = findViewById(R.id.cameraButton);
        input = findViewById(R.id.editText);


        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryIntent();
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
            }
        });
    }

    private void galleryIntent() {
        // Get image from gallery
        Intent getImage = new Intent(Intent.ACTION_PICK);
        getImage.setType("image/*");
        startActivityForResult(getImage, REQUEST_IMAGE_Gallery);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void cameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Save full size image
        try {
            File file = createImageFile();
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e) {
            Toast.makeText(this, e + "", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private Uri saveImage() {
        if (capturedImage != null) {
            if (input.length() > 0) {
                this.appendMessage();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "stegano");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                try {
                    OutputStream output = getContentResolver().openOutputStream(uri);
                    capturedImage.compress(Bitmap.CompressFormat.PNG, 100, output);
                    output.close();
                    Toast.makeText(getApplicationContext(), "Saved image to " + uri.getPath(), Toast.LENGTH_LONG).show();
                    return uri;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "No text entered!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image captured!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void appendMessage() {
        String msg = input.getText().toString();
        int counter = 0;
        for (char c :
                msg.toCharArray()) {
            int j = counter / capturedImage.getWidth();
            int i = counter % capturedImage.getWidth();
            int color = capturedImage.getPixel(i, j);
            Log.i("encode", String.format("i(%d),j(%d): %s", i, j, color));
            capturedImage.setPixel(i, j,
                    Color.argb(c + 120, Color.red(color), Color.green(color), Color.blue(color)));
            counter++;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // Set image which taken from camera to ImageView
            Bitmap originalImage = BitmapFactory.decodeFile(currentPhotoPath);
            capturedImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            capturedImage.setHasAlpha(true);
            image.setImageBitmap(originalImage);
        } else if (requestCode == REQUEST_IMAGE_Gallery && resultCode == RESULT_OK) {

            // Set image which taken from gallery to ImageView
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                capturedImage = selectedImage.copy(Bitmap.Config.ARGB_8888, true);
                capturedImage.setHasAlpha(true);
                image.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permission list
                String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, REQUEST_ID);
            } else {
                cameraIntent();
            }
        } else {
            cameraIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID) {
            // If permission access
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                cameraIntent();
            } else {
                Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}