package com.example.best_of_best.login_file;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.best_of_best.R;
import com.example.best_of_best.login;
import com.example.best_of_best.db_java.login_member;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class login_membership extends AppCompatActivity {

    EditText ed_signName, ed_signId, ed_signBirth, ed_signBirth2, ed_signBirth3, ed_signPw1,
            ed_signPw2, ed_signmail;
    Button bt_signId, bt_checkPw, bt_memberSucces;
    String name, id, pw, email, birth;

    boolean pw_flag = true, id_flag = true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_membership);

        ed_signName = findViewById(R.id.ed_signName);
        ed_signId = findViewById(R.id.ed_signId);

        ed_signPw1 = findViewById(R.id.ed_signPw1);
        ed_signPw2 = findViewById(R.id.ed_signPw2);
        ed_signmail = findViewById(R.id.ed_signmail);

        ed_signBirth = findViewById(R.id.ed_signBirth);
        ed_signBirth2 = findViewById(R.id.ed_signBirth2);
        ed_signBirth3 = findViewById(R.id.ed_signBirth3);

        bt_signId = findViewById(R.id.bt_signId);
        bt_checkPw = findViewById(R.id.bt_checkPw);
        bt_memberSucces = findViewById(R.id.bt_memberSucces);

        Button test = findViewById(R.id.test_bt);
        DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();

        // 아이디 중복
        bt_signId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = ed_signId.getText().toString();
                if(!ed_signId.getText().toString().equals(""))
                    mRootRef.child("member").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                login_member mem = snapshot1.getValue(login_member.class);
                                String str = mem.getId();
                                if(str.equals(id)) {
                                    Toast.makeText(getApplicationContext(), "존재하는 아이디", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            Toast.makeText(getApplicationContext(), "사용가능 아이디", Toast.LENGTH_SHORT).show();
                            id_flag = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                else Toast.makeText(getApplicationContext(), "아이디 입력", Toast.LENGTH_SHORT).show();
            }
        });

        // 비밀번호 확인 후 버튼 비활성화
        bt_checkPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str1 = ed_signPw1.getText().toString();
                String str2 = ed_signPw2.getText().toString();

                if(str1.equals(str2) && !str1.equals("")){
                    Toast.makeText(getApplicationContext(), "사용가능한 비밀번호", Toast.LENGTH_SHORT).show();
                    pw = str1; pw_flag = false;
                    bt_checkPw.setEnabled(false);
                }else{
                    Toast.makeText(getApplicationContext(), "비밀번호를 다시 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bt_memberSucces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = ed_signName.getText().toString();
                id = ed_signId.getText().toString();
                email = ed_signmail.getText().toString();
                Toast.makeText(getApplicationContext(), email, Toast.LENGTH_SHORT).show();

                boolean birth_flag = true;
                if(!ed_signBirth.getText().toString().equals("") || !ed_signBirth.getText().toString().equals("") || !ed_signBirth.getText().toString().equals("")){
                    StringBuilder sb = new StringBuilder();
                    sb.append(ed_signBirth.getText().toString() + "-" + ed_signBirth2.getText().toString() + "-" + ed_signBirth3.getText().toString());
                    birth = String.valueOf(sb);
                    birth_flag = false;

                }


                if(name.equals("") || id.equals("") || email.equals("") || birth_flag || pw_flag || id_flag) {

                    Toast.makeText(getApplicationContext(), "입력되지않는 창이 존재합니다", Toast.LENGTH_SHORT).show();

                }else{
                    // db 연결
                    mRootRef.child("member").child(id).setValue(new login_member(name, id, pw, email, birth));
                    Toast.makeText(getApplicationContext(), "회원가입 완료", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), login.class));
                    finish();
                }

            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), ed_signmail.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
