package com.example.best_of_best.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.best_of_best.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.concurrent.ExecutionException;

public class Fragment_mlkit_poseDetect extends AppCompatActivity {

    private static final String TAG = "CameraXLivePreview";

    private PreviewView previewView;
    private Button stop, start;
    private EditText test_deit;
    private FloatingActionButton fab;

    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable private Preview previewUseCase;
    @Nullable private ImageAnalysis analysisUseCase;

    private PoseDetectorOptions options;
    private PoseDetector poseDetector;


    private int lensFacing = CameraSelector.LENS_FACING_FRONT;

    private CameraSelector cameraSelector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pose_detect);

        previewView = findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        stop = findViewById(R.id.stopButton);
        start = findViewById(R.id.startButton);
        test_deit = findViewById(R.id.test_edit);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraSelector != null) lenschange();
            }
        });

        try {
            cameraProvider = ProcessCameraProvider.getInstance(this).get();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(Fragment_mlkit_poseDetect.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    previewView.setVisibility(View.VISIBLE);

                    bindPreView();
                    bindAnalysis();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraProvider.unbindAll();
                previewView.setVisibility(View.INVISIBLE);
//                onPause();
//                cameraProvider.unbindAll();
            }
        });

    }

    private void bindPreView(){
        if (cameraProvider == null) return;
        if (previewUseCase != null) cameraProvider.unbind(previewUseCase);


        previewUseCase = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3) //디폴트 표준 비율
                .build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase);
    }

    private void bindAnalysis(){
        if (cameraProvider == null) return;
        if (analysisUseCase != null) cameraProvider.unbind(analysisUseCase);


        try {
            // detect 부분
            options = new PoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                    .build();
            poseDetector = PoseDetection.getClient(options);

        } catch (Exception e) {
//            Log.e("CameraXLivePreview", "Can not create image processor: " + selectedModel, e);
            Toast.makeText(
                            getApplicationContext(),
                            "Can not create image processor: " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }



        analysisUseCase = new ImageAnalysis.Builder().build();

        boolean needOverlay = true;
        analysisUseCase.setAnalyzer(
                ContextCompat.getMainExecutor(this),
                image -> {
                    Image mediaImage = image.getImage();
//                    Toast.makeText(getApplicationContext(), "sdsd", Toast.LENGTH_SHORT).show();

                    if(mediaImage != null){
                        InputImage de_image =
                                InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());

                        PoseDetectorOptions options =
                                new PoseDetectorOptions.Builder()
                                        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                                        .build();
                        PoseDetector poseDetector = PoseDetection.getClient(options);

//                        Toast.makeText(getApplicationContext(), "asdx", Toast.LENGTH_SHORT).show();
                        Task<Pose> result =
                            poseDetector.process(de_image)
                                .addOnSuccessListener(
                                    new OnSuccessListener<Pose>() {
                                        @Override
                                        public void onSuccess(Pose pose) {

                                            try {
                                                PoseLandmark a = pose.getPoseLandmark(20);
                                                PoseLandmark b = pose.getPoseLandmark(22);

                                                float a_x = a.getPosition().x;
                                                float a_y = a.getPosition().y;


                                                test_deit.setText(a_x + ", " + a_y);

                                                if(a_x > 100.0){
                                                    Intent intent = new Intent(getApplicationContext(), pop.class);
                                                    intent.putExtra("data", "Test Popup");
                                                    startActivityForResult(intent, 1);
                                                    cameraProvider.unbindAll();
                                                    previewView.setVisibility(View.INVISIBLE);
                                                }

                                            }catch (Exception e){
                                                Log.e("AuthException ERROR: {} ", String.valueOf(e)); //로그남김
                                            }
                                            image.close();
//
                                        }
                                    })
                                .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...

                                            Toast.makeText(getApplicationContext(), "pose detect fail", Toast.LENGTH_SHORT).show();

                                        }

                                    });
                    }
                }

        );

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //데이터 받기
                String result = data.getStringExtra("result");
//                txtResult.setText(result);
            }
        }
    }
    protected void onPause() {
        super.onPause();
//        if (imageProcessor != null) {
//            imageProcessor.stop();
//        }
    }

    public void lenschange(){
        if(cameraSelector == null){
            return;
        }
        int newLensFacing =
                lensFacing == CameraSelector.LENS_FACING_FRONT
                        ? CameraSelector.LENS_FACING_BACK
                        : CameraSelector.LENS_FACING_FRONT;
        CameraSelector newCameraSelector =
                new CameraSelector.Builder().requireLensFacing(newLensFacing).build();
        try {
            if (cameraProvider.hasCamera(newCameraSelector)) {
                Log.d(TAG, "Set facing to " + newLensFacing);
                lensFacing = newLensFacing;
                cameraSelector = newCameraSelector;
                cameraProvider.unbindAll();
                bindPreView();
                bindAnalysis();
                return;
            }
        } catch (CameraInfoUnavailableException e) {
            // Falls through
        }
    }
}
