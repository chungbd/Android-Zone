package com.example.democamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Flash;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener,  ImageCallback  {

    private final static CameraLogger LOG = CameraLogger.create("DemoApp");
    private PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private AppCompatImageView imageView;
    private CameraView camera;
    private FloatingActionButton fabFlash;
    boolean isFlash = false;
    private TextView textNumberPicture;
    int numberPicture;

    private ArrayList<String> images;
    private ArrayList<String> imagesThumnails;
    ImageCallback callBackImage;
    CustomProgressBar customProgressBar;
    boolean isShowProgress = false;

    ImageWorker imageWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);
        camera = findViewById(R.id.cameraView);
        if(this.hasNoPermissions()){
            new Thread( new Runnable() { @Override public void run() {
                ActivityCompat.requestPermissions(
                        CameraActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1000
                );
            }
            }
            );
        } else {
            camera.open();
        }
        camera.setLifecycleOwner(this);
        camera.addCameraListener(new Listener());
        fabFlash = findViewById(R.id.fab_flash);
        imageView = findViewById(R.id.image_view);
        textNumberPicture = findViewById(R.id.numberPicture);
        images = new ArrayList<String>();
        imagesThumnails = new ArrayList<String>();
        callBackImage = this;
        customProgressBar = new CustomProgressBar(this);
        permissionsDelegate.requestCameraPermission();
//        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        findViewById(R.id.fab_camera).setOnClickListener(this);
        findViewById(R.id.fab_flash).setOnClickListener(this);
//        findViewById(R.id.fab_done).setOnClickListener(this);
//        findViewById(R.id.fab_cancel).setOnClickListener(this);


        File directory = this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File myDir = new File(directory + "/PTI");
        myDir.mkdirs();

        imageWorker = new ImageWorker(myDir);

        imageWorker.setCompleteProcess(new ProcessHanded() {
            @Override
            public void onProcessing(boolean status) {
                Log.i("setCompleteProcess",String.valueOf(status));
            }
        });

        imageWorker.setOnCompleteImageProcess(new ImageProcessHanded() {

            @Override
            public void onComplete(@NotNull ImageInfor info) {
                Log.i("setCompleteProcess",String.valueOf(info));
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    private Boolean hasNoPermissions() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }
    private void message(@NonNull String content, boolean important) {
        if (important) {
            LOG.w(content);
            Toast.makeText(this, content, Toast.LENGTH_LONG).show();
        } else {
            LOG.i(content);
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
        }
    }


    private class Listener extends CameraListener {

        @Override
        public void onCameraOpened(@NonNull CameraOptions options) {

        }

        @Override
        public void onCameraError(@NonNull CameraException exception) {
            super.onCameraError(exception);
            message("Got CameraException #" + exception.getReason(), true);
        }

        @Override
        public void onPictureTaken(@NonNull PictureResult result) {
            super.onPictureTaken(result);
            try {
                result.toBitmap(new BitmapCallback() {

                    @Override
                    public void onBitmapReady(final Bitmap bitmap) {
                        imageWorker.addBitmap(bitmap);

                        numberPicture += 1;
                        textNumberPicture.setText(String.valueOf(numberPicture));
                        imageView.setImageBitmap(bitmap);

                    }
                });
            } catch (UnsupportedOperationException e) {
            }

        }

        @Override
        public void onVideoTaken(@NonNull VideoResult result) {
            super.onVideoTaken(result);
        }

        @Override
        public void onVideoRecordingStart() {
            super.onVideoRecordingStart();
        }

        @Override
        public void onVideoRecordingEnd() {
            super.onVideoRecordingEnd();
        }

        @Override
        public void onExposureCorrectionChanged(float newValue, @NonNull float[] bounds, @Nullable PointF[] fingers) {
            super.onExposureCorrectionChanged(newValue, bounds, fingers);
        }

        @Override
        public void onZoomChanged(float newValue, @NonNull float[] bounds, @Nullable PointF[] fingers) {
            super.onZoomChanged(newValue, bounds, fingers);
        }
    }

    private void converImageThumbnails(Bitmap bitmap){
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/PTI");
        myDir.mkdirs();

        String fname = String.valueOf(System.currentTimeMillis()) +".jpeg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.flush();
            out.close();
            imagesThumnails.add(file.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_flash:
                updateFlash();
                break;
            case R.id.fab_camera:
                capturePicture();
                break;
//            case R.id.fab_cancel:
//                finish();
//                onBackPressed();
//                break;
//            case R.id.fab_done:
//                sendToMain(1);
//                break;
        }
    }

    private void updateFlash(){
        this.isFlash = !this.isFlash;
        if(this.isFlash == true) {
            fabFlash.setImageResource(R.drawable.ic_flash);
            this.camera.setFlash(Flash.ON);
        } else {
            fabFlash.setImageResource(R.drawable.ic_flash_off);
            this.camera.setFlash(Flash.OFF);
        }

    }
    private void capturePicture() {
//        if (camera.isTakingPicture() || this.numberPicture > 14) return;

        camera.takePicture();

    }

    private void  sendToMain(int resultcode) {
        if(images.size() == numberPicture){
            Intent intent = getIntent();
            Bundle bundle =new Bundle();
            bundle.putStringArrayList("images",images);
            intent.putExtra("bundle", bundle);
            setResult(resultcode, intent); // phương thức này sẽ trả kết quả cho Activity1
            finish();
            onBackPressed();
        } else {
            isShowProgress = true;
            customProgressBar.show();
            Toast.makeText(this, "Vui lòng chờ một lát hình ảnh đang được xử lý", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void  callbackImage(String url){
        images.add(url);
        if(isShowProgress == true){
            if(images.size() == numberPicture){
                customProgressBar.dismiss();
                Intent intent = getIntent();
                Bundle bundle =new Bundle();
                bundle.putStringArrayList("images",images);
//                bundle.putStringArrayList("imagesThumnails",imagesThumnails);
                intent.putExtra("bundle", bundle);
                setResult(1, intent); // phương thức này sẽ trả kết quả cho Activity1
                finish();
                onBackPressed();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            camera.open();
        }
    }

    private void saveImage(Bitmap bitmap,File fileImage) {
        ScaleBitmap.scaleBitmap(fileImage);
        images.add(fileImage.getPath());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        images = null;
        camera = null;
        customProgressBar = null;
    }
}

interface ImageCallback{
    public void callbackImage(String url);
}
