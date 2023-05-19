package com.example.best_of_best;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.best_of_best.db_java.db_daily;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "CameraXLivePreview";

    private PreviewView previewView;
    private Button stop, start;
    private EditText test_deit;
    private TextView txtResult;

    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable private Preview previewUseCase;
    @Nullable private ImageAnalysis analysisUseCase;

    private PoseDetectorOptions options;
    private PoseDetector poseDetector;


    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraSelector cameraSelector;
    private boolean flag;
    // 이미지 회전각도 계산 하는 변수
    private InputImage in_image;

    //갯수 카운팅 하는 변수
    private int Push_Count = 0;
    private int Squrt_Count = 0;
    private int Pullup_Count = 0;

    //갯수 카운팅 더하는 변수
    private int Add_Push_Count = 0;
    private int Add_Squrt_Count = 0;
    private int Add_Pullup_Count = 0;

    // 동작 시작 전
    private boolean isPullUpInProgress = false;
    private boolean isSquatInProgress  = false;
    private boolean isPushUpInProgress = false;

    // 받아오는 변수
    private String user_id;
    private String event;
    private String set;
    private String count;

    // 셋트 카운팅
    private int set_counting = 1;
    private int Int_Set,Int_count;

    //현재 시간
    private String now = LocalDate.now().toString();

    // database 값 불러옴
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

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
        txtResult = (TextView)findViewById(R.id.test_edit);



        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();





        try {
            cameraProvider = ProcessCameraProvider.getInstance(this).get();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        user_id = intent.getStringExtra("id");
        event = intent.getStringExtra("event");
        set = intent.getStringExtra("set");
        count = intent.getStringExtra("count");



        Int_Set = Integer.parseInt(set);
        Int_count = Integer.parseInt(count);
        System.out.println(user_id + ", "+ event + ", " + set + ", " + count);


        // 인식하면서 개수 카운트


        // 종료시 db 저장
        // db 넣는부분
        daily_insert(user_id, event, set, count);




        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    previewView.setVisibility(View.VISIBLE);
                    // 1초 동안 스레드 정지
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
                Toast.makeText(getApplicationContext(), "End", Toast.LENGTH_SHORT).show();
                // 디비 넘겨주세요.
                if(event.equals("Push_up")){
                    mRootRef.child("daily").child("11").child(now).child("Push_up").setValue(new db_daily("Push_up", "set_counting", "Add_Push_Count"));
                    finish();
                }
                if(event.equals("Pull_up")){
                    mRootRef.child("daily").child("11").child(now).child("Pull_up").setValue(new db_daily("Pull_up", "set_counting", "Add_Push_Count"));
                    finish();
                }
                if(event.equals("Sqrt")){
                    mRootRef.child("daily").child("11").child(now).child("Sqrt").setValue(new db_daily("Sqrt", "set_counting", "Add_Squrt_Count"));
                    finish();
                }

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
                                                            if(Int_Set >= set_counting){

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


                                                                if(event.equals("Push_up")){
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
                                                                }
                                                                else if(event.equals("Pull_up")){
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
                                                                }

                                                                else if(event.equals("Squrt")){
                                                                    //모두 계산이 끝난 후 DB에 올릴 예정
    //                                                              //무릎이랑 엉덩이 비교해서 시작
                                                                    if(right_knee_y < right_hip_y && left_knee_y < left_hip_y && !isSquatInProgress){
                                                                        isSquatInProgress = true;
                                                                    }
                                                                    if(right_knee_y > right_hip_y && left_knee_y > left_hip_y && isSquatInProgress){
                                                                        Squrt_Count++;
                                                                        isSquatInProgress = false;
                                                                        System.out.println(set + ", " + count+"3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333");
                                                                        test_deit.setText("스쿼트"+Squrt_Count);
                                                                    }
                                                                }
                                                                else{
                                                                    Toast.makeText(getApplicationContext(), "운동이 없습니다.", Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                }

                                                                Add_Push_Count += Pullup_Count;
                                                                Add_Squrt_Count += Squrt_Count;
                                                                Add_Push_Count += Push_Count;

                                                                if(Pullup_Count==Int_count || Squrt_Count==Int_count || Push_Count==Int_count){

                                                                    // 디비 넘겨주세요.
                                                                    if(event.equals("Push_up")){
                                                                        mRootRef.child("daily").child("11").child(now).child("Push_up").setValue(new db_daily("Push_up", "set_counting", "Add_Push_Count"));
                                                                    }
                                                                    if(event.equals("Pull_up")){
                                                                        mRootRef.child("daily").child("11").child(now).child("Pull_up").setValue(new db_daily("Pull_up", "set_counting", "Add_Push_Count"));
                                                                    }
                                                                    if(event.equals("Sqrt")){
                                                                        mRootRef.child("daily").child("11").child(now).child("Sqrt").setValue(new db_daily("Sqrt", "set_counting", "Add_Squrt_Count"));
                                                                    }
                                                                    set_counting++;

                                                                    Pullup_Count = 0;
                                                                    Squrt_Count = 0;
                                                                    Push_Count = 0;
                                                                }

                                                            }
                                                            else {
                                                                Toast.makeText(getApplicationContext(), "할당량이 끝났습니다.", Toast.LENGTH_SHORT).show();
                                                                cameraProvider.unbindAll();
                                                                previewView.setVisibility(View.INVISIBLE);
                                                                finish();
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
//                                                System.out.println("dfdfdff");
                                                        Toast.makeText(getApplicationContext(), "pose detect fail", Toast.LENGTH_SHORT).show();
//                                                Log.d(TAG, "pose detcetion fail");
                                                    }

                                                });
                    }
                }


        );

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1) {
//            if (resultCode == RESULT_OK) {
//                //데이터 받기
//                String result = data.getStringExtra("result");
//                txtResult.setText(result);
//            }
//        }
//    }

    protected void onPause() {
        super.onPause();
//        if (imageProcessor != null) {
//            imageProcessor.stop();
//        }
    }

    private void daily_insert(String id, String event, String set, String count){
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        String now = LocalDate.now().toString();

        mRootRef.child("daily").child(id).child(now).child(event).setValue(new db_daily(event, set, count));

    }
}