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
    Button saveBtn;
    EditText input;
    Bundle extra;
    Bitmap capturedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        // Set variables values
        image = findViewById(R.id.encodeImageView);
        saveBtn = findViewById(R.id.saveButton);
        input = findViewById(R.id.messageEditText);

        getExtra();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
            }
        });
    }

    private void getExtra() {
        extra = getIntent().getExtras();
        if (extra.get("image") != null) {
            Bitmap originalImage = BitmapFactory.decodeFile(String.valueOf(extra.get("image")));
            // Copy image and change mutable status
            capturedImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            capturedImage.setHasAlpha(true);
            // Set image to ImageView
            image.setImageBitmap(capturedImage);
        } else if (extra.get("imageUri") != null) {
            Uri imageUri = (Uri) extra.get("imageUri");
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                // Copy image and change mutable status
                capturedImage = selectedImage.copy(Bitmap.Config.ARGB_8888, true);
                capturedImage.setHasAlpha(true);
                // Set image to ImageView
                image.setImageBitmap(capturedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImage() {
        if (capturedImage != null) {
            if (input.length() > 0) {
                appendMessage();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "stegano");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                try {
                    OutputStream output = getContentResolver().openOutputStream(uri);
                    capturedImage.compress(Bitmap.CompressFormat.PNG, 100, output);
                    output.close();
                    Toast.makeText(getApplicationContext(), "Saved image to " + uri.getPath(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "No text entered!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image captured!", Toast.LENGTH_SHORT).show();
        }
    }

    private void appendMessage() {
        if (capturedImage != null) {
            String msg = input.getText().toString() + "$";
            int counter = 0;
            for (char c :
                    msg.toCharArray()) {
                int j = counter / capturedImage.getHeight();
                int i = counter % capturedImage.getWidth();
                int color = capturedImage.getPixel(i, j);

                capturedImage.setPixel(i, j,
                        Color.argb(c, Color.red(color), Color.green(color), Color.blue(color)));
                counter++;
            }
            Toast.makeText(this, "Message successfully encoded", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No image captured!", Toast.LENGTH_SHORT).show();
        }
    }
}