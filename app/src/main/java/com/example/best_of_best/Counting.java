package com.example.best_of_best;

import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.Nullable;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.concurrent.ExecutionException;

public class Counting extends AppCompatActivity {

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

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    private String Test_str = "push";

    //갯수 카운팅 하는 변수
    private int Push_Count = 0;
    private int Squrt_Count = 0;
    private int Pullup_Count = 0;

    // 동작 시작 전
    private boolean isPullUpInProgress = false;
    private boolean isSquatInProgress  = false;
    private boolean isPushUpInProgress = false;

    // 필요한 좌표값들
    // 풀업
    float left_wrist_y = 0;
    float right_wrist_y = 0;
    float left_shoulder_y = 0;
    float right_shoulder_y = 0;

    // 스쿼트
    float right_knee_y = 0;
    float left_knee_y = 0;
    float right_hip_y = 0;
    float left_hip_y = 0;


    // 푸쉬업
    float right_elbow_y = 0;
    float left_elbow_y = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pose_ddetect);

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
                if (ActivityCompat.checkSelfPermission(Counting.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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


                                                            // 모두 계산이 끝난 후 DB에 올릴 예정
                                                            // 손목이랑 어깨 y축만 비교해서 계산하기(풀업)
                                                            if(left_elbow_y > left_shoulder_y && right_elbow_y > right_shoulder_y && !isPullUpInProgress){
                                                                isPullUpInProgress = true;
                                                            }
                                                            if(right_elbow_y < right_shoulder_y && left_elbow_y < left_shoulder_y && isPullUpInProgress){
                                                                Pullup_Count++;
                                                                isPullUpInProgress = false;
                                                                test_deit.setText("풀업"+Pullup_Count);
                                                            }

//                                                             모두 계산이 끝난 후 DB에 올릴 예정
//                                                             무릎이랑 엉덩이 비교해서 시작
                                                            if(right_knee_y < right_hip_y && left_knee_y < left_hip_y && !isSquatInProgress){
                                                                isSquatInProgress = true;
                                                            }
                                                            if(right_knee_y > right_hip_y && left_knee_y > left_hip_y && isSquatInProgress){
                                                                Squrt_Count++;
                                                                isSquatInProgress = false;
                                                                test_deit.setText("스쿼트"+Squrt_Count);
                                                            }

                                                            // 어깨가 팔꿈치 기준 (푸쉬업 동작 시작)
                                                            if (right_ankle_y < right_shoulder_y && left_ankle_y < left_shoulder_y && !isPushUpInProgress) {
                                                                isPushUpInProgress = true;
                                                            }
//                                                            System.out.println(right_ankle_y+ "," +right_shoulder_y);
//                                                            test_deit.setText(right_ankle_y+ "," +right_shoulder_y);

                                                            // 양쪽 손목이 양쪽 어깨와 비슷한 높이로 올라간 경우 (푸쉬업 동작 종료)
                                                            if (right_ankle_y+15 > right_shoulder_y && left_ankle_y+15 > left_shoulder_y && isPushUpInProgress) {
                                                                Push_Count++;
                                                                isPushUpInProgress = false;
                                                                test_deit.setText("푸쉬업"+Push_Count);
                                                            }

//

                                                            if(Pullup_Count>10 || Squrt_Count>10 || Push_Count>10){

                                                                cameraProvider.unbindAll();
                                                                previewView.setVisibility(View.INVISIBLE);
                                                                Toast.makeText(getApplicationContext(), "End", Toast.LENGTH_SHORT).show();
                                                            }

                                                            }catch (Exception e){
                                                                Log.e("AuthException ERROR: {} ", String.valueOf(e)); //로그남김
                                                            }
                                                            image.close();


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
//        //Stop 버튼을 누르면 DB에 올라 가겠끔
//        Stop_Btn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                if(Test_str.equals("pu_up")){
////                                                            System.out.println("pu_up");
//                    LocalDate now = LocalDate.now();
//
//                    // 값 넘겨 받와야함.(풀업)
//                    String id = "11";
//
//                    mRootRef.child("daily").child(id).child(String.valueOf(now)).addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//                                String xx = snapshot1.getValue(String.class);
//
//                                if (Test_str.equals(xx)) {
//                                    // db에 운동이 존재한다면
//                                    // 운동, 세트, 개수 추가
//                                    // ++++++
//                                } else {
////                                                                             없다면 새로 추가
////                                                                             String 종목
////                                                                             int 세트 개수
////                                                                             db_daily daily = new db_daily(종목, 세트, 개수);
////                                                                             mRootRef.child("daily").child(id).child(String.valueOf(now)).child(Test_str).setValue(daily);
//                                }
//
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                    // 갯수 카운팅
//                }else if(Test_str.equals("push")){
////                                                            System.out.println("push");
//                    LocalDate now = LocalDate.now();
//
//                    // 값 넘겨 받와야함.(푸쉬업)
//                    String id = "11";
//
//                    mRootRef.child("daily").child(id).child(String.valueOf(now)).addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//                                String xx = snapshot1.getValue(String.class);
//
//                                if (Test_str.equals(xx)) {
//                                    // db에 운동이 존재한다면
//                                    // 운동, 세트, 개수 추가
//                                    // ++++++
//                                } else {
////                                                                             없다면 새로 추가
////                                                                             String 종목
////                                                                             int 세트 개수
////                                                                             db_daily daily = new db_daily(종목, 세트, 개수);
////                                                                             mRootRef.child("daily").child(id).child(String.valueOf(now)).child(Test_str).setValue(daily);
//                                }
//
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                }else if(Test_str.equals("sq")){
////                                                            System.out.println("sq");
//                    LocalDate now = LocalDate.now();
//
//                    // 값 넘겨 받와야함.(스쿼트)
//                    String id = "11";
//
//                    mRootRef.child("daily").child(id).child(String.valueOf(now)).addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//                                String xx = snapshot1.getValue(String.class);
//
//                                if (Test_str.equals(xx)) {
//                                    // db에 운동이 존재한다면
//                                    // 운동, 세트, 개수 추가
//                                    // ++++++
//                                } else {
////                                                                             없다면 새로 추가
////                                                                             String 종목
////                                                                             int 세트 개수
////                                                                             db_daily daily = new db_daily(종목, 세트, 개수);
////                                                                             mRootRef.child("daily").child(id).child(String.valueOf(now)).child(Test_str).setValue(daily);
//                                }
//
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                }
//            }
//        });

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
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
