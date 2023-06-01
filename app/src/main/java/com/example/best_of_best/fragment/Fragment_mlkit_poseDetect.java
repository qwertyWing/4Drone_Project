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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.best_of_best.R;
import com.example.best_of_best.db_java.db_daily;
import com.example.best_of_best.db_java.login_member;
import com.example.best_of_best.home;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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

    private String mem_id = "";
    private login_member mem;
//    private static boolean detect_flag = true;
//    private static String str_event = "";

    private int Pullup_Division = 0, Squrt_Division = 0, Pushup_Division = 0;
    private int asdc = 0;

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

        // 카메라 setting
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        // 카메라 전후방 전환
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

        // flag_search > id 값 받아오기
        Intent get_id_intent = getIntent();

        mem_id = get_id_intent.getStringExtra("id");
        mem = (login_member) get_id_intent.getSerializableExtra("object");
        Toast.makeText(getApplicationContext(), "ef" + mem.getId(), Toast.LENGTH_SHORT).show();

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
//            options = new PoseDetectorOptions.Builder()
//                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
//                    .build();
//            poseDetector = PoseDetection.getClient(options);
            PoseDetectorOptions options = new PoseDetectorOptions.Builder()
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
                                                PoseLandmark left_shoulder = pose.getPoseLandmark(11);
                                                PoseLandmark right_shoulder = pose.getPoseLandmark(12);
                                                PoseLandmark left_elbow = pose.getPoseLandmark(13);
                                                PoseLandmark right_elbow = pose.getPoseLandmark(14);
                                                PoseLandmark left_wrist = pose.getPoseLandmark(15);
                                                PoseLandmark right_wrist = pose.getPoseLandmark(16);
                                                PoseLandmark left_hip = pose.getPoseLandmark(23);
                                                PoseLandmark right_hip = pose.getPoseLandmark(24);
                                                PoseLandmark left_knee = pose.getPoseLandmark(25);
                                                PoseLandmark right_knee = pose.getPoseLandmark(26);
                                                PoseLandmark left_ankle = pose.getPoseLandmark(27);
                                                PoseLandmark right_ankle = pose.getPoseLandmark(28);

                                                float left_shoulder_c = left_shoulder.getInFrameLikelihood();
                                                float right_shoulder_c = right_shoulder.getInFrameLikelihood();
                                                float left_elbow_c = left_elbow.getInFrameLikelihood();
                                                float right_elbow_c = right_elbow.getInFrameLikelihood();
                                                float left_wrist_c = left_wrist.getInFrameLikelihood();
                                                float right_wrist_c = right_wrist.getInFrameLikelihood();
                                                float left_hip_c = left_hip.getInFrameLikelihood();
                                                float right_hip_c = right_hip.getInFrameLikelihood();
                                                float left_knee_c = left_knee.getInFrameLikelihood();
                                                float right_knee_c = right_knee.getInFrameLikelihood();
                                                float left_ankle_c = left_ankle.getInFrameLikelihood();
                                                float right_ankle_c = right_ankle.getInFrameLikelihood();

                                                float pose_hood[] = {
                                                        left_shoulder_c, right_shoulder_c, left_elbow_c, right_elbow_c, left_wrist_c, right_wrist_c,
                                                        left_hip_c, right_hip_c, left_knee_c, right_knee_c, left_ankle_c, right_ankle_c
                                                };

                                                float left_shoulder_x = left_shoulder.getPosition().x;
                                                float right_shoulder_x = right_shoulder.getPosition().x;
                                                float left_elbow_x = left_elbow.getPosition().x;
                                                float right_elbow_x = right_elbow.getPosition().x;
                                                float left_wrist_x = left_wrist.getPosition().x;
                                                float right_wrist_x = right_wrist.getPosition().x;
                                                float left_hip_x = left_hip.getPosition().x;
                                                float right_hip_x = right_hip.getPosition().x;
                                                float left_knee_x = left_knee.getPosition().x;
                                                float right_knee_x = right_knee.getPosition().x;


                                                float left_shoulder_y = left_shoulder.getPosition().y;
                                                float right_shoulder_y = right_shoulder.getPosition().y;
                                                float left_elbow_y = left_elbow.getPosition().y;
                                                float right_elbow_y = right_elbow.getPosition().y;
                                                float left_wrist_y = left_wrist.getPosition().y;
                                                float right_wrist_y = right_wrist.getPosition().y;
                                                float left_hip_y = left_hip.getPosition().y;
                                                float right_hip_y = right_hip.getPosition().y;
                                                float left_knee_y = left_knee.getPosition().y;
                                                float right_knee_y = right_knee.getPosition().y;
                                                float left_ankle_y = left_ankle.getPosition().y;
                                                float right_ankle_y = right_ankle.getPosition().y;

//                                                float input [][] = {{left_shoulder_x,left_shoulder_y,right_shoulder_x,right_shoulder_y,left_elbow_x,left_elbow_y,
//                                                        right_elbow_x,right_elbow_y,left_wrist_x,left_wrist_y,right_wrist_x,right_wrist_y,left_hip_x,left_hip_y,
//                                                        right_hip_x,right_hip_y,left_knee_x,left_knee_y,right_knee_x,right_knee_y}};
//                                                float[] output = new float[]{0};
//                                                Interpreter tflite = getTfliteInterpreter("helpme_model.tflite");
//                                                tflite.run(input,output);

                                                // 예측된 클래스 중 가장 높은 확률을 가진 클래스 인덱스 찾기
//                                                int predictedClass = argmax(output);
                                                // flag 사용 > 처음인식, 인신된 후 운동 조작
                                                // 구현 해야됨.
                                                if (confidence(pose_hood)){
//                                                    asdc++;
//                                                    test_deit.setText("push_up");
//                                                    if (asdc >= 120) {
//                                                        // 푸쉬업
//                                                        cameraProvider.unbindAll();
//                                                        previewView.setVisibility(View.INVISIBLE);
//
//                                                        Intent intent = new Intent(getApplicationContext(), pop.class);
//                                                        intent.putExtra("event", "Push_up");
//                                                        intent.putExtra("id", mem_id);
//                                                        intent.putExtra("object", mem);
//                                                        startActivityForResult(intent, 1);
//                                                        finish();
//                                                    } else {
////                                                        Pullup_Division = 0;
////                                                        Squrt_Division = 0;
//
////                                                            test_deit.setText("p_reset");
//                                                    }
                                                    if (right_shoulder_y < right_wrist_y && left_shoulder_y < left_wrist_y) {
//                                                    test_deit.setText("t4");
//                                                        if(predictedClass == 1) {
                                                        Pullup_Division++;
//                                                        test_deit.setText(String.format(Locale.US, "%.2f", left_wrist.getInFrameLikelihood()));
                                                        test_deit.setText("squat");
//                                                        test_deit.setText("pull_up");
//                                                        // #################################
//                                                        Thread.sleep(5000);
//                                                        Intent intent = new Intent(getApplicationContext(), pop.class);
//                                                        intent.putExtra("event", "Pull_up");
//                                                        intent.putExtra("object", mem);
//                                                        intent.putExtra("id", mem_id);
//
//                                                        startActivityForResult(intent, 1);
//                                                        finish();
//                                                        // ###################################

                                                        if (Pullup_Division >= 120) {
                                                            // 스퀏
                                                            cameraProvider.unbindAll();
                                                            previewView.setVisibility(View.INVISIBLE);

                                                            Intent intent = new Intent(getApplicationContext(), pop.class);
                                                            intent.putExtra("event", "Sqrt");
                                                            intent.putExtra("object", mem);
                                                            intent.putExtra("id", mem_id);

                                                            startActivityForResult(intent, 1);
                                                            finish();
                                                        } else {
                                                            Squrt_Division = 0;
                                                            Pushup_Division = 0;
//                                                            test_deit.setText("P_reset");
                                                        }
//                                                    } else if(predictedClass == 2) {
                                                    } else if (left_wrist_y == left_ankle_y && right_wrist_y == right_ankle_y) {
                                                        Pushup_Division++;
                                                        test_deit.setText("push_up");
                                                        if (Pushup_Division >= 120) {
                                                            // 푸쉬업
                                                            cameraProvider.unbindAll();
                                                            previewView.setVisibility(View.INVISIBLE);

                                                            Intent intent = new Intent(getApplicationContext(), pop.class);
                                                            intent.putExtra("event", "Push_up");
                                                            intent.putExtra("id", mem_id);
                                                            intent.putExtra("object", mem);
                                                            startActivityForResult(intent, 1);
                                                            finish();
                                                        } else {
                                                            Pullup_Division = 0;
                                                            Squrt_Division = 0;
//                                                            test_deit.setText("p_reset");
                                                        }
//                                                    }else if(predictedClass == 3){
                                                    } else if (right_knee_y > right_hip_y && left_knee_y > left_hip_y) {
                                                        Squrt_Division++;
                                                        test_deit.setText("pull_up");
//                                                        test_deit.setText("squrt");
                                                        if (Squrt_Division >= 120) {
                                                            // 스쿼트
                                                            cameraProvider.unbindAll();
                                                            previewView.setVisibility(View.INVISIBLE);

                                                            Intent intent = new Intent(getApplicationContext(), pop.class);
                                                            intent.putExtra("event", "Pull_up");
                                                            intent.putExtra("id", mem_id);
                                                            intent.putExtra("object", mem);
                                                            startActivityForResult(intent, 1);
                                                            finish();
                                                        } else {
                                                            Pullup_Division = 0;
                                                            Pushup_Division = 0;
//                                                            test_deit.setText("s_reset");
                                                        }
                                                    }
                                              }
//
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

    private void lenschange(){
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
//    private static boolean value_flag = true;
    private void daily_insert(String id, String event, String set, String count){
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        String now = LocalDate.now().toString();

        mRootRef.child("daily").child(id).child(now).child(event).setValue(new db_daily(event, set, count));

    }

    private boolean confidence(float arr[]){
        for(float value : arr){
            if(value <0.8) return false;
        }

        return true;
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(Fragment_mlkit_poseDetect.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // argmax 함수 정의 (가장 큰 값의 인덱스를 반환)
    private static int argmax(float[] array) {
        int maxIndex = 0;
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

}
