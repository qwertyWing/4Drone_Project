package com.example.best_of_best;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.best_of_best.db_java.db_daily;
import com.example.best_of_best.db_java.login_member;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


public class main extends AppCompatActivity {
//    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    static int count = 0;
    private static boolean value_flag = true;

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

        String now = LocalDate.now().toString();

        // database 값 저장
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRootRef.child("daily").child("11").child(now).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            Toast.makeText(getApplicationContext(), "sdsd", Toast.LENGTH_SHORT).show();
                            db_daily da = snapshot1.getValue(db_daily.class);
//                            Toast.makeText(getApplicationContext(),  da.getEvent() +", "+da.getCount()+", " + da.getSet(), Toast.LENGTH_SHORT).show();

                            String st = "push_up";
                            if(st.equals(da.getEvent())){
                                Toast.makeText(getApplicationContext(),  da.getEvent() +", "+da.getCount()+", " + da.getSet(), Toast.LENGTH_SHORT).show();
                                break;
//                                int nw_set = Integer.parseInt(da.getSet()) + 3;
//                                int nw_count = Integer.parseInt(da.getCount()) + 5;
//
//                                da.setCount(String.valueOf(nw_count));
//                                da.setSet(String.valueOf(nw_set));
//
//                                Map<String, Object> update = new HashMap<>();
//                                update.put("/daily/"+"11"+"/"+now+"/"+"push_up"+"/", da);
//
//                                mRootRef.updateChildren(update);
//                                value_flag = true;

                            }


                                // db_daily 객체로 변환된 데이터를 사용합니다.

//                            if("push_up".equals(da.getEvent())){
//                                int nw_set = da.getSet() + 5;
//                                int nw_count = da.getCount() + 15;
//
//                                da.setCount(nw_count);
//                                da.setSet(nw_set);
//
//                                Map<String, Object> update = new HashMap<>();
//                                update.put("/daily/"+"11"+"/"+now+"/", da);
//
//                                mRootRef.updateChildren(update);
//                                value_flag = false;
//                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                if(value_flag) {
                    mRootRef.child("daily").child("11").child(now).child("wd").setValue(new db_daily("sacw", "5", "15"));
                    value_flag=false;
                }
                // now 현재값 존재 여부
                //
//                  mRootRef.child("daily").child("id").child(String.valueOf(now)).child("풀업").setValue(new db_daily("풀업", 5, 12) );
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
