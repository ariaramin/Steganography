package com.example.steganography;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class DecodeActivity extends AppCompatActivity {

    // Initialize variables
    ImageView image;
    Button decodeBtn, galleryBtn;
    TextView txtMessage;
    Bitmap capturedImage;

    static final int REQUEST_IMAGE_Gallery = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        image = findViewById(R.id.imageView);
        decodeBtn = findViewById(R.id.decodeButton);
        galleryBtn = findViewById(R.id.galleryButton);
        txtMessage = findViewById(R.id.messageTextView);


        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryIntent();
            }
        });

        decodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decodeMessage();
            }
        });
    }

    private void galleryIntent() {
        // Get image from gallery
        Intent getImage = new Intent(Intent.ACTION_PICK);
        getImage.setType("image/*");
        startActivityForResult(getImage, REQUEST_IMAGE_Gallery);
    }

    public void decodeMessage() {
        if (capturedImage != null) {
            String message = "";
            for (int j = 0; j < capturedImage.getHeight(); j++) {
                for (int i = 0; i < capturedImage.getWidth(); i++) {
                    int color = capturedImage.getPixel(i, j);
                    char alpha = (char) (Color.alpha(color) - 120);
                    Log.i("decode", String.format("i(%d),j(%d): %s", i, j, alpha));
                    if (alpha == '$') {
                        final String finalMessage = message;
                        txtMessage.setText(finalMessage);
                    } else {
                        message += alpha;
                    }
                }
            }
        } else {
            Toast.makeText(this, "No image captured!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_Gallery && resultCode == RESULT_OK) {

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
}