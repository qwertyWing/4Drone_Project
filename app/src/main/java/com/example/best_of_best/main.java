package com.example.best_of_best;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.best_of_best.db_java.db_daily;
import com.example.best_of_best.db_java.login_member;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;

public class main extends AppCompatActivity {
//    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    static int count = 0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button bt = findViewById(R.id.t_db_btn);
        Button bt1 = findViewById(R.id.t_db_btn1);
        EditText dt = findViewById(R.id.t_db_edittext);

        // 데이터베이스(firebase)
        // 연동된 db를 FirebaseDatabase.getInstance().getReference()를사용하여 connect

//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference ref = database.getReference("widget");

        // database 값 불러옴
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
//        mRootRef.child("daily").setValue(new login_member("1", "2", "3", "4", "5"));

//        mRootRef.child("test").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot snapshot1 : snapshot.getChildren()){
//
////                    Toast.makeText(getApplicationContext(), ""+snapshot1, Toast.LENGTH_LONG).show();
//                    count++;
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
////                Toast.makeText(getApplicationContext(), ""+error, Toast.LENGTH_LONG).show();
//            }
//        });


        // database 값 저장
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalDate now = LocalDate.now();

                // now 현재값 존재 여부
                //
                mRootRef.child("daily").child("id").child(String.valueOf(now)).child("풀업").setValue(new db_daily("풀업", 5, 12) );
            }
        });

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootRef.child("test").child("dsf").child("da").setValue(new login_member("dsf", "dsf", "dsf", "dsf", "dsf"));
            }
        });

    }
}
