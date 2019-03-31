package com.mepco.mepcoauth;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final int requestCode = 20;
    private ImageView imageHolder;
    Uri image;
    String mCameraFileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnClick = findViewById(R.id.btnClick);
        imageHolder = findViewById(R.id.imgCamPic);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            btnClick.setText("Allow Permssions");
        }

        btnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA}, 2);
                }
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                }
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                }

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    Intent intent = new Intent();
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                    Date date = new Date();
                    DateFormat df = new SimpleDateFormat("-mm-ss");

                    String newPicFile = df.format(date) + ".jpg";
                    String outPath = "/sdcard/" + newPicFile;
                    File outFile = new File(outPath);

                    mCameraFileName = outFile.toString();
                    Uri outuri = Uri.fromFile(outFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                    startActivityForResult(intent, 2);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2) {
                if (data != null) {
                    image = data.getData();
                }
                if (image == null && mCameraFileName != null) {
                    image = Uri.fromFile(new File(mCameraFileName));
                }
                Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
                ExifInterface exif;
                try {
                    exif = new ExifInterface(image.getPath());
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

                Matrix matrix = new Matrix();
                matrix.preRotate(270);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                imageHolder.setImageBitmap(bitmap);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                FaceDetector faceDetector = new
                        FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                        .build();
                if (!faceDetector.isOperational()) {
                    Toast.makeText(this, "Error in Face Detector", Toast.LENGTH_SHORT).show();
                    return;
                }
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<Face> faces = faceDetector.detect(frame);
                Face thisFace;
                new File(image.getPath()).delete();
                try {
                    thisFace = faces.valueAt(0);
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                int x1 = (int) thisFace.getPosition().x;
                int y1 = (int) thisFace.getPosition().y;
                int x2 = (int) thisFace.getWidth();
                int y2 = (int) thisFace.getHeight();
                Bitmap bMap = Bitmap.createBitmap(bitmap, x1, y1, x2, y2);
                imageHolder.setImageBitmap(bMap);
                findViewById(R.id.btnClick).setEnabled(false);

            }
        }
    }
}
