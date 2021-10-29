package com.example.steganography;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class DecodeActivity extends AppCompatActivity {

    // Initialize variables
    ImageView image;
    Button decodeBtn;
    TextView messageText;
    Bitmap capturedImage;
    Bundle extra;

    static final int REQUEST_IMAGE_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        image = findViewById(R.id.decodeImageView);
        decodeBtn = findViewById(R.id.decodeMessageButton);
        messageText = findViewById(R.id.messageTextView);

        getExtra();

        decodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decodeMessage();
            }
        });
    }

    private void getExtra() {
        extra = getIntent().getExtras();
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

    public void decodeMessage() {
        if (capturedImage != null) {
            String message = "";
            for (int j = 0; j < capturedImage.getHeight(); j++) {
                for (int i = 0; i < capturedImage.getWidth(); i++) {
                    int color = capturedImage.getPixel(i, j);
                    char alpha = (char) Color.alpha(color);
                    if (alpha == '$') {
                        messageText.setText(message);
                        Toast.makeText(this, "Message successfully decoded", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        message += alpha;
                    }
                }
            }
        } else {
            Toast.makeText(this, "No image captured!", Toast.LENGTH_SHORT).show();
        }
    }
}