package com.example.best_of_best;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.best_of_best.db_java.login_member;
import com.example.best_of_best.login_file.login_membership;
import com.example.best_of_best.login_file.login_pwsearch;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Button btn_login = findViewById(R.id.bt_login);

        Button btn_idsearch = findViewById(R.id.bt_idsearch);
        Button btn_pwsearch = findViewById(R.id.bt_pwsearch);
        Button btn_membership = findViewById(R.id.bt_membership);

        EditText ed_id = findViewById(R.id.ed_id);
        EditText ed_pw = findViewById(R.id.ed_pw);

        btn_login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                String id = ed_id.getText().toString();
                String pw = ed_pw.getText().toString();
                if(id.equals("") || pw.equals("")){
                    Toast.makeText(getApplicationContext(), "아이디 or 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                }else{
                    mRootRef.child("member").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                login_member mem = snapshot1.getValue(login_member.class);

                                if(id.equals(mem.getId())) {
                                    if(pw.equals(mem.getPw())){
                                        Intent intent = new Intent(getApplicationContext(), home.class);
                                        intent.putExtra("object", mem);
                                        startActivity(intent);
                                        finish();
                                        break;
                                    }else{
                                        Toast.makeText(getApplicationContext(), "입력하신 정보가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w(TAG, "loadPost:onCancelled", error.toException());
                        }
                    });
                }
            }
        });

        btn_idsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btn_membership.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent member_intent = new Intent(getApplicationContext(), login_membership.class);
                startActivity(member_intent);
//                finish();
            }
        });

        btn_pwsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), login_pwsearch.class));
//                finish();
            }
        });
    }

}