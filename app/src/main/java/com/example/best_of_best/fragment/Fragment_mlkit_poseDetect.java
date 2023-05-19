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

import java.time.LocalDate;
import java.util.HashMap;
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
    private static boolean detect_flag = true;
    private static String str_event = "";

    private int Pullup_Division, Squrt_Division, Pushup_Division;
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

                                                float left_shoulder_y = left_shoulder.getPosition().y;
                                                float right_shoulder_y = right_shoulder.getPosition().y;
                                                float left_elbow_y = left_elbow.getPosition().y;
                                                float right_elbow_y = right_elbow.getPosition().y;
                                                float left_wrist_y = left_wrist.getPosition().y;
                                                float right_wrist_y = right_wrist.getPosition().y;;
                                                float left_hip_y = left_hip.getPosition().y;
                                                float right_hip_y = right_hip.getPosition().y;
                                                float left_knee_y = left_knee.getPosition().y;
                                                float right_knee_y = right_knee.getPosition().y;

                                                float left_ankle_y = left_ankle.getPosition().y;
                                                float right_ankle_y = right_ankle.getPosition().y;


                                                // flag 사용 > 처음인식, 인신된 후 운동 조작
                                                // 구현 해야됨.
                                                if(detect_flag) {
                                                    if (right_shoulder_y < right_wrist_y && left_shoulder_y < left_wrist_y) {
                                                        Pullup_Division++;
                                                        if(Pullup_Division >= 20){
                                                            // 풀업
                                                            cameraProvider.unbindAll();
                                                            previewView.setVisibility(View.INVISIBLE);

                                                            Intent intent = new Intent(getApplicationContext(), pop.class);
                                                            intent.putExtra("id", mem_id);
                                                            startActivityForResult(intent, 1);
                                                            finish();
                                                        }
                                                        else{
                                                            Squrt_Division = 0;
                                                            Pushup_Division = 0;
                                                        }

                                                    }
                                                    else if(left_wrist_y == left_ankle_y && right_wrist_y == right_ankle_y){
                                                        Pushup_Division++;
                                                        if(Pushup_Division >= 20){
                                                            // 풀업
                                                            cameraProvider.unbindAll();
                                                            previewView.setVisibility(View.INVISIBLE);

                                                            Intent intent = new Intent(getApplicationContext(), pop.class);
                                                            intent.putExtra("id", mem_id);
                                                            startActivityForResult(intent, 1);
                                                            finish();
                                                        }
                                                        else{
                                                            Pullup_Division = 0;
                                                            Squrt_Division = 0;
                                                        }
                                                    }
                                                    else if(right_knee_y > right_hip_y && left_knee_y > left_hip_y){
                                                        Squrt_Division++;
                                                        if(Squrt_Division >= 20){
                                                            // 풀업
                                                            cameraProvider.unbindAll();
                                                            previewView.setVisibility(View.INVISIBLE);

                                                            Intent intent = new Intent(getApplicationContext(), pop.class);
                                                            intent.putExtra("id", mem_id);
                                                            startActivityForResult(intent, 1);
                                                            finish();
                                                        }
                                                        else{
                                                            Pullup_Division = 0;
                                                            Pushup_Division = 0;
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
    private void daily_event_Check(String id, String event){

    }


    // daily 접근 코드
//    private static boolean value_flag = true;
    private void daily_insert(String id, String event, String set, String count){
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        String now = LocalDate.now().toString();
//        String now = "2023-05-15";
        mRootRef.child("daily").child(id).child(now).child(event).setValue(new db_daily(event, set, count));
//        mRootRef.child("daily").child(id).child(now).child(event).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot snapshot1 : snapshot.getChildren()){
//                    db_daily da = snapshot1.getValue(db_daily.class);
//
//                    Toast.makeText(getApplicationContext(), "asdfeve", Toast.LENGTH_SHORT).show();
//                    if(event.equals(da.getEvent())){
//                        int nw_set = da.getSet() + set;
//                        int nw_count = da.getCount() + count;
//
//                        da.setCount(nw_count);
//                        da.setSet(nw_set);
//
//                        Map<String, Object> update = new HashMap<>();
//                        update.put("/daily/"+id+"/"+now+"/", da);
//
//                        mRootRef.updateChildren(update);
//                        value_flag = false;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        if(value_flag) {
//            mRootRef.child("daily").child(id).child(now).child(event).setValue(new db_daily(event, set, count));
//        }
    }
}
